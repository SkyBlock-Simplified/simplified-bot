package dev.sbs.simplifiedbot.util;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.impl.hypixel.exception.HypixelApiException;
import dev.sbs.api.client.impl.hypixel.request.HypixelRequest;
import dev.sbs.api.client.impl.hypixel.response.skyblock.SkyBlockAuctionsEndedResponse;
import dev.sbs.api.client.impl.hypixel.response.skyblock.SkyBlockAuctionsResponse;
import dev.sbs.api.client.impl.hypixel.response.skyblock.SkyBlockBazaarResponse;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.SkyBlockAuction;
import dev.sbs.api.client.impl.hypixel.response.skyblock.implementation.SkyBlockBazaarProduct;
import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.data.model.skyblock.items.ItemModel;
import dev.sbs.api.minecraft.nbt.tags.primitive.StringTag;
import dev.sbs.api.scheduler.Scheduler;
import dev.sbs.api.util.SimplifiedException;
import dev.sbs.discordapi.util.exception.DiscordException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Getter
public class ItemCache {

    private final AuctionHouse auctionHouse = new AuctionHouse();
    private final Bazaar bazaar = new Bazaar();
    private final EndedAuctions endedAuctions = new EndedAuctions();

    public final double getPrice(ItemModel itemModel) {
        return this.getPrice(itemModel, 3);
    }

    public final double getPrice(ItemModel itemModel, int averageWith) {
        return this.getPrice(itemModel, averageWith, false);
    }

    public final double getPrice(ItemModel itemModel, int averageWith, boolean recentHistory) {
        Optional<SkyBlockBazaarProduct> bazaarItem = this.getBazaar()
            .getItems()
            .stream()
            .filter(product -> product.getItemId().equals(itemModel.getItemId()))
            .findFirst();

        double price;

        if (bazaarItem.isPresent())
            price = bazaarItem.get().getQuickStatus().getBuyPrice();
        else {
            if (recentHistory) {
                ConcurrentList<SkyBlockAuction.Ended> endedAuctions = this.getEndedAuctions()
                    .getItems()
                    .stream()
                    .filter(endedAuction -> itemModel.getItemId().equals(
                            endedAuction.getItemNbt()
                                .getNbtData()
                                .getPathOrDefault("tag.ExtraAttributes.id", StringTag.EMPTY)
                                .getValue()
                    ))
                    .collect(Concurrent.toList())
                    .sorted(endedAuction -> endedAuction.getTimestamp().getRealTime());

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
                    .sorted(activeAuction -> activeAuction.getEndsAt().getRealTime());

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
                HypixelRequest hypixelRequest = SimplifiedApi.getApiRequest(HypixelRequest.class);
                ConcurrentList<SkyBlockAuction> auctions = Concurrent.newList();
                SkyBlockAuctionsResponse skyBlockAuctionsResponse = hypixelRequest.getAuctions();
                auctions.addAll(skyBlockAuctionsResponse.getAuctions());
                long lastUpdated = skyBlockAuctionsResponse.getLastUpdated().getRealTime();

                // Handle Auction Pages
                for (int i = 1; i < skyBlockAuctionsResponse.getTotalPages(); i++) {
                    try {
                        SkyBlockAuctionsResponse skyBlockAuctionsPageResponse = hypixelRequest.getAuctions(i);
                        auctions.addAll(skyBlockAuctionsPageResponse.getAuctions());

                        if (skyBlockAuctionsPageResponse.getLastUpdated().getRealTime() > lastUpdated)
                            lastUpdated = skyBlockAuctionsPageResponse.getLastUpdated().getRealTime();
                    } catch (HypixelApiException hypixelApiException) {
                        if (hypixelApiException.getStatus().getCode() == 404)
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

    public static class Bazaar extends Cache<SkyBlockBazaarProduct> {

        @Override
        public long process() {
            SkyBlockBazaarResponse skyBlockBazaarResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getBazaar();
            this.replaceItems(skyBlockBazaarResponse.getProducts().values());
            return skyBlockBazaarResponse.getLastUpdated().getRealTime();
        }

    }

    public static class EndedAuctions extends Cache<SkyBlockAuction.Ended> {

        @Override
        protected long process() {
            SkyBlockAuctionsEndedResponse skyBlockAuctionsEndedResponse = SimplifiedApi.getApiRequest(HypixelRequest.class).getEndedAuctions();
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
