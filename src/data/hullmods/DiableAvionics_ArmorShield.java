package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;


import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.scripts.Diableavionics_ArmorShieldSubsystem;
import data.shipsystems.scripts.Diablevaionics_ArmorShieldSubsystemV2;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;
import org.magiclib.subsystems.examples.FormationSwitchPDDroneSubsystem;
import org.magiclib.subsystems.examples.PDDroneSubsystem;

import java.awt.*;
import java.util.Map;

import static data.scripts.util.Diableavionics_stringsManager.txt;


public class DiableAvionics_ArmorShield extends BaseHullMod {

    public static final float HULL_BONUS = 30f;


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        final Color green = new Color(55,245,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float padQuote = 10f;
        final float padSig = 2f;
        final float padS = 0f;

        int ShieldHP= (int) Global.getSettings().getHullSpec("diableavionics_strifeShield").getHitpoints();
        int ShieldArmor=(int)Global.getSettings().getHullSpec("diableavionics_strifeShield").getArmorRating();
        tooltip.addSectionHeading(txt("hm_tooltip_title_specific"), Alignment.MID, pad);



        tooltip.addPara(txt("hm_armorshield_01"),Misc.getHighlightColor(),pad);

        tooltip.addPara(txt("hm_armorshield_02")
                           + txt("hm_armorshield_03"),
                pad,Misc.getHighlightColor(),
                String.valueOf(ShieldHP),String.valueOf(ShieldArmor),"30","30%");



        tooltip.addPara("%s", padQuote, flavor, txt("hm_armorshield_05")).italicize();
        tooltip.addPara("%s", padSig, flavor, txt("hm_armorshield_06"));



    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }


    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

        if (fighter.getWing().getSpec().getId().contains("strife")){
            MagicSubsystemsManager.addSubsystemToShip(fighter, new Diableavionics_ArmorShieldSubsystem(fighter));




//              MagicSubsystemsManager.addSubsystemToShip(fighter, new Diablevaionics_ArmorShieldSubsystemV2(fighter));
        }
    }
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {

      return true;

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        return null;
    }



    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        return null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return null;
    }


}
