

package data.scripts.ai;

        import com.fs.starfarer.api.Global;
        import com.fs.starfarer.api.combat.*;
        import com.fs.starfarer.api.impl.campaign.ids.Stats;

        import java.awt.*;
        import java.lang.annotation.Target;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        import com.fs.starfarer.api.util.IntervalUtil;
        import data.scripts.util.Diableavionics_finder;
        import org.lazywizard.lazylib.MathUtils;
        import org.lazywizard.lazylib.VectorUtils;
        import org.lazywizard.lazylib.combat.AIUtils;
        import org.lazywizard.lazylib.combat.CombatUtils;
        import org.lwjgl.util.vector.Vector2f;


public class Diableavionics_antiMissileLauncherMidAI implements AutofireAIPlugin {

    private final IntervalUtil Scantimer = new IntervalUtil(0.8f,1.2f);
    private final IntervalUtil Alertimer = new IntervalUtil(2f,2f);
    private final WeaponAPI weapon;
    private final ShipAPI ship;
    private CombatEntityAPI target = null;
    private boolean shouldfire=false;
    private Vector2f targetlocation=null;
    private MissileAPI targetmissile;
    private ShipAPI targetfighter;


    private List<ShipAPI> fighters = new ArrayList<>();
    private  List<MissileAPI> missiles;

    private boolean Isalert = false;
    private boolean shouldFire = false;



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

        if(Global.getCombatEngine().isPaused()) return;

        if(ship.getFluxTracker().isOverloadedOrVenting()) return;

        if(weapon.isDisabled()) return;



        if(Isalert){
            Alertimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(Alertimer.intervalElapsed()){
                Isalert=false;

                // 在结束警报前 再对场上进行一次扫描 检查是否有导弹存在
                // 17/12/2025 这个策略会导致导弹舱过量发射导弹
//                    missiles = AIUtils.getNearbyEnemyMissiles(ship,weapon.getRange());
//                    if(!missiles.isEmpty()) {Isalert=true;}
            }
            return;
        }

        Scantimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if(Scantimer.intervalElapsed()) {

            targetfighter = null;
            targetmissile = null;


            missiles = AIUtils.getNearbyEnemyMissiles(ship, weapon.getRange());
            // reset fighters
            fighters = new ArrayList<>();
            for(ShipAPI enemy:AIUtils.getNearbyEnemies(ship,weapon.getRange())){
                if(!enemy.isFighter()) continue;
                fighters.add(enemy);
            }


            if (missiles.isEmpty()&&fighters.isEmpty()) {
                Isalert=false;
            } else {
                Isalert=true;

                float a=999999;
                float b=999999;
                //should`t be possibe but if
                if(!missiles.isEmpty()){
                    targetmissile=missiles.get(0);
                    b = MathUtils.getDistance(targetmissile,ship);
                }

                if(!fighters.isEmpty()){
                    targetfighter=fighters.get(0);
                    a = MathUtils.getDistance(targetfighter, ship);
                }


                if(a<=b){
                    targetlocation=targetfighter.getLocation();
                }else{
                    targetlocation=targetmissile.getLocation();
                }

            }




        }
    }

    @Override
    public boolean shouldFire() {

        if(Isalert) return true;

        return false;

    }

    @Override
    public void forceOff() {
        this.shouldFire = false;
    }

    @Override
    public Vector2f getTarget() {

        if (targetfighter != null || targetmissile != null)
            return targetlocation;
        return null;

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
