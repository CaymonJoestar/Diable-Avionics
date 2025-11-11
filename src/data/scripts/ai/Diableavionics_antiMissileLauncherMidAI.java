

package data.scripts.ai;

        import com.fs.starfarer.api.Global;
        import com.fs.starfarer.api.combat.*;
        import com.fs.starfarer.api.impl.campaign.ids.Stats;

        import java.awt.*;
        import java.lang.annotation.Target;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import com.fs.starfarer.api.util.IntervalUtil;
        import data.scripts.util.Diableavionics_finder;
        import org.lazywizard.lazylib.VectorUtils;
        import org.lazywizard.lazylib.combat.AIUtils;
        import org.lwjgl.util.vector.Vector2f;


public class Diableavionics_antiMissileLauncherMidAI implements AutofireAIPlugin {

    private final IntervalUtil Scantimer = new IntervalUtil(0.5f,1f);
    private final IntervalUtil Alertimer = new IntervalUtil(2f,2f);
    private final WeaponAPI weapon;
    private final ShipAPI ship;
    private CombatEntityAPI target = null;
    private boolean shouldfire=false;
    private Vector2f targetlocation;
    private MissileAPI targetmissile;
    private ShipAPI targetfighter = null;
    private boolean Isalert = false;
    private  List<MissileAPI> missiles;
    public Diableavionics_antiMissileLauncherMidAI(WeaponAPI weapon){
        this.weapon=weapon;
        this.ship= weapon.getShip();
    }


    //for medium size magicbox launcher

    @Override
    public void advance(float amount) {


        //对于舰队防空式的导弹 我们采取一个警戒-响应策略：
        //发射器平时不开火，保持警戒，每隔一定时间进行遍历，一旦侦测到射程内出现导弹就持续开火若干轮，然后继续警戒
        //发射时不指定目标，减少遍历次数，同时导弹自己会有分配目标的代码，避免了所有导弹打一个目标的情形。



        if(!Global.getCombatEngine().isPaused()){

            if(weapon.isDisabled()) {return;}

            if(Isalert){
                Alertimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if(Alertimer.intervalElapsed()){
                    Isalert=false;

                    //在结束警报前 再对场上进行一次扫描 检查是否有导弹存在
                    missiles = AIUtils.getNearbyEnemyMissiles(ship,weapon.getRange());
                    if(!missiles.isEmpty()) {Isalert=true;}
                }
                return;
            }

            Scantimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(Scantimer.intervalElapsed()) {

                targetfighter = null;
                targetmissile = null;


                missiles = AIUtils.getNearbyEnemyMissiles(ship, weapon.getRange());

                if (missiles.isEmpty()) {

                } else {
                    Isalert=true;
                }
            }

        }
    }

    @Override
    public boolean shouldFire() {

        if(Isalert) {return true;}
        else if(targetfighter!=null||targetmissile!=null)
        {
            return  true;
        }else {
            return false;
        }
    }

    @Override
    public void forceOff() {

    }

    @Override
    public Vector2f getTarget() {


        if(targetmissile==null){
            if(targetfighter!=null){
                return targetfighter.getLocation();
            }else {
                return null;
            }
        }else {
            return targetmissile.getLocation();
        }

    }

    @Override
    public ShipAPI getTargetShip() {
        return targetfighter;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {

        if(targetmissile!=null){
            return  targetmissile;
        }else {
            return null;
        }
    }
}
