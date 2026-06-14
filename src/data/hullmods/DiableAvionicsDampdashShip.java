package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.scripts.Diableavionics_assumeControlStats2;
import data.shipsystems.scripts.Diableavionics_dampdashSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;

import java.awt.*;

import static data.scripts.util.Diableavionics_stringsManager.txt;


public class DiableAvionicsDampdashShip extends BaseHullMod {


    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float padQuote = 10f;
        final float padSig = 2f;
        final float padS = 0f;

        tooltip.addSectionHeading(txt("hm_tooltip_title_specific"), Alignment.MID, pad);


        tooltip.addPara(
                txt("hm_dampdash_01")
                        + "\n"
                        + txt("hm_dampdash_02")
                        + txt("hm_dampdash_03")
                        + txt("hm_dampdash_04")
                        + txt("hm_dampdash_05"),
                pad, Misc.getHighlightColor(),
                " 5s "," 1 "," 15s "," 50% ");

        tooltip.addPara(txt("hm_dampdash_06"), Misc.getHighlightColor(),pad);
        tooltip.addSectionHeading(txt("hm_dampdash_07"), Alignment.MID, pad);
        //final TooltipMakerAPI incompat_text = tooltip.beginImageWithText("graphics/icons/tooltip/vanilla_marine.png", 32f);
        //incompat_text.addPara("Raid effectiveness will be depended on %s", padS, Misc.getHighlightColor(), new String[] { "the amount of marines onboard your fleet." });
        tooltip.addPara(txt("hm_dampdash_08"),pad);

        tooltip.addPara("%s", padQuote, flavor, txt("hm_dampdash_09")).italicize();
        tooltip.addPara("%s", padSig, flavor, txt("hm_dampdash_10"));

    }



    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id){
        //fighter.getHullSpec().addBuiltInMod("diableavionics_dampdashwanzer");


        if (fighter.getWing().getSpec().getTags().contains("wanzer")){
            MagicSubsystemsManager.addSubsystemToShip(fighter, new Diableavionics_dampdashSubsystem(fighter));
        }

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return (ship.getHullSpec().getHullId().startsWith("diable"));
    }
}
