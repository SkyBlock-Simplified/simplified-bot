package dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_item_craftables;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.skyblock.items.ItemModel;
import dev.sbs.simplifiedbot.data.skyblock.shop_data.shop_bit_items.ShopBitItemModel;

public interface ShopBitItemCraftableModel extends Model {

    ShopBitItemModel getBitItem();

    ItemModel getCraftableItem();

    String getDescription();

    String getExpression();

}
