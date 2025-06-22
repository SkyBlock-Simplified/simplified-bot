package dev.sbs.simplifiedbot.data.skyblock.pet_data.pets;

import dev.sbs.api.data.model.Model;
import dev.sbs.simplifiedbot.data.discord.emojis.EmojiModel;
import dev.sbs.simplifiedbot.data.skyblock.pet_data.pet_types.PetTypeModel;
import dev.sbs.simplifiedbot.data.skyblock.rarities.RarityModel;
import dev.sbs.simplifiedbot.data.skyblock.skills.SkillModel;

public interface PetModel extends Model {

    String getKey();

    String getName();

    RarityModel getLowestRarity();

    SkillModel getSkill();

    PetTypeModel getPetType();

    EmojiModel getEmoji();

    Integer getMaxLevel();

    String getSkin();

}
