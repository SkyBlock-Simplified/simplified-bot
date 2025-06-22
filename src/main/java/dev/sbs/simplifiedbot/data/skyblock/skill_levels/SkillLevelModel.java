package dev.sbs.simplifiedbot.data.skyblock.skill_levels;

import dev.sbs.api.data.model.BuffEffectsModel;
import dev.sbs.simplifiedbot.data.skyblock.skills.SkillModel;

import java.util.List;

public interface SkillLevelModel extends BuffEffectsModel<Double, Double> {

    SkillModel getSkill();

    Integer getLevel();

    Double getTotalExpRequired();

    List<String> getUnlocks();

}
