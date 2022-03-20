package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.exception.HypixelApiException;
import dev.sbs.api.client.hypixel.implementation.HypixelSkyBlockData;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockAuction;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockAuctionsEndedResponse;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockAuctionsResponse;
import dev.sbs.api.client.hypixel.response.skyblock.SkyBlockBazaarResponse;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.scheduler.Scheduler;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.api.util.collection.concurrent.Concurrent;
import dev.sbs.api.util.collection.concurrent.ConcurrentList;
import dev.sbs.discordapi.util.exception.DiscordException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class ItemCache {

    @Getter private final AuctionHouse auctionHouse = new AuctionHouse();
    @Getter private final Bazaar bazaar = new Bazaar();
    @Getter private final EndedAuctions endedAuctions = new EndedAuctions();

    public final double getPrice(ItemModel itemModel) {
        return this.getPrice(itemModel, 3);
    }

    public final double getPrice(ItemModel itemModel, int averageWith) {
        return this.getPrice(itemModel, averageWith, false);
    }

    public final double getPrice(ItemModel itemModel, int averageWith, boolean recentHistory) {
        Optional<SkyBlockBazaarResponse.Product> bazaarItem = this.getBazaar()
            .getItems()
            .stream()
            .filter(product -> product.getItem().equals(itemModel))
            .findFirst();

        double price;

        if (bazaarItem.isPresent())
            price = bazaarItem.get().getQuickStatus().getBuyPrice();
        else {
            if (recentHistory) {
                ConcurrentList<SkyBlockAuctionsEndedResponse.EndedAuction> endedAuctions = this.getEndedAuctions()
                    .getItems()
                    .stream()
                    .filter(endedAuction -> itemModel.getItemId().equals(
                            endedAuction.getItemNbt()
                                .getNbtData()
                                .getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY)
                                .getValue()
                    ))
                    .collect(Concurrent.toList())
                    .sort(endedAuction -> endedAuction.getTimestamp().getRealTime());

                // Calculate Recent Price
                double totalPrice = IntStream.range(0, averageWith)
                    .mapToDouble(index -> endedAuctions.get(index).getPrice())
                    .sum();
                price = totalPrice / averageWith;
            } else {
                ConcurrentList<SkyBlockAuction> activeAuctions = this.getAuctionHouse()
                    .getItems()
                    .stream()
                    .filter(activeAuction -> itemModel.getItemId().equals(
                        activeAuction.getItemNbt()
                            .getNbtData()
                            .getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY)
                            .getValue()
                    ))
                    .collect(Concurrent.toList())
                    .sort(activeAuction -> activeAuction.getEndsAt().getRealTime());

                // Calculate Current Price
                double totalPrice = IntStream.range(0, averageWith)
                    .mapToDouble(index -> activeAuctions.get(index).getHighestBid())
                    .sum();
                price = totalPrice / averageWith;
            }
        }

        return price;
    }

    public static class AuctionHouse extends Cache<SkyBlockAuction> {

        @Override
        public long process() {
            try {
                HypixelSkyBlockData hypixelSkyBlockData = SimplifiedApi.getWebApi(HypixelSkyBlockData.class);
                ConcurrentList<SkyBlockAuction> auctions = Concurrent.newList();
                SkyBlockAuctionsResponse skyBlockAuctionsResponse = hypixelSkyBlockData.getAuctions();
                auctions.addAll(skyBlockAuctionsResponse.getAuctions());
                long lastUpdated = skyBlockAuctionsResponse.getLastUpdated().getRealTime();

                // Handle Auction Pages
                for (int i = 1; i < skyBlockAuctionsResponse.getTotalPages(); i++) {
                    try {
                        SkyBlockAuctionsResponse skyBlockAuctionsPageResponse = hypixelSkyBlockData.getAuctions(i);
                        auctions.addAll(skyBlockAuctionsPageResponse.getAuctions());

                        if (skyBlockAuctionsPageResponse.getLastUpdated().getRealTime() > lastUpdated)
                            lastUpdated = skyBlockAuctionsPageResponse.getLastUpdated().getRealTime();
                    } catch (HypixelApiException hypixelApiException) {
                        if (hypixelApiException.getHttpStatus().getCode() == 404)
                            break;
                        else
                            throw hypixelApiException;
                    }
                }

                this.replaceItems(auctions);
                return lastUpdated;
            } catch (Exception exception) {
                throw SimplifiedException.of(DiscordException.class)
                    .withMessage("Unable to update entire Auction House cache!")
                    .withCause(exception)
                    .build();
            }
        }

    }

    public static class Bazaar extends Cache<SkyBlockBazaarResponse.Product> {

        @Override
        public long process() {
            SkyBlockBazaarResponse skyBlockBazaarResponse = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getBazaar();
            this.replaceItems(skyBlockBazaarResponse.getProducts().values());
            return skyBlockBazaarResponse.getLastUpdated().getRealTime();
        }

    }

    public static class EndedAuctions extends Cache<SkyBlockAuctionsEndedResponse.EndedAuction> {

        @Override
        protected long process() {
            SkyBlockAuctionsEndedResponse skyBlockAuctionsEndedResponse = SimplifiedApi.getWebApi(HypixelSkyBlockData.class).getEndedAuctions();
            this.replaceItems(skyBlockAuctionsEndedResponse.getAuctions());
            return skyBlockAuctionsEndedResponse.getLastUpdated().getRealTime();
        }

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static abstract class Cache<T> {

        @Getter private long lastUpdated;
        @Getter private long nextUpdate;
        private final ConcurrentList<T> items = Concurrent.newList();
        private final AtomicBoolean replacing = new AtomicBoolean();
        private final AtomicBoolean updating = new AtomicBoolean();

        public final ConcurrentList<T> getItems() {
            while (this.replacing.get())
                Scheduler.sleep(1);

            return Concurrent.newUnmodifiableList(this.items);
        }

        public final boolean isExpired() {
            return System.currentTimeMillis() >= this.getNextUpdate();
        }

        public final boolean isUpdating() {
            return this.updating.get();
        }

        protected void replaceItems(Collection<T> items) {
            this.replacing.set(true);
            this.items.clear();
            this.items.addAll(items);
            this.replacing.set(false);
        }

        protected abstract long process();

        public final void update() {
            if (this.isExpired() && !this.isUpdating()) {
                this.updating.set(true);
                long nextUpdate = this.process();
                this.updating.set(false);
                this.lastUpdated = System.currentTimeMillis();
                this.nextUpdate = nextUpdate + (2 * 60 * 1000);
            }
        }

    }

}
