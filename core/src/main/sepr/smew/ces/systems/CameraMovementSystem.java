package sepr.smew.ces.systems;

import sepr.smew.ces.components.*;
import sepr.smew.ces.entities.*;
import sepr.smew.util.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.ashley.utils.ImmutableArray;

import com.badlogic.gdx.graphics.OrthographicCamera;

import java.lang.Math;

import java.util.ArrayList;


public class CameraMovementSystem extends EntitySystem implements EntityListener {
    private ComponentMapper<PhysicsComponent> pm = ComponentMapper.getFor(PhysicsComponent.class);
    private ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);
    private ComponentMapper<MapComponent> mm = ComponentMapper.getFor(MapComponent.class);
    
    private CameraEntity cameraEntity;
    private SmewEntity smewEntity;
    private MapEntity mapEntity;

    protected Engine engine;
    private RoomBound currentBound;

    public CameraMovementSystem(int priority) {
        super(priority);
        this.currentBound = null;
    }
    
    @Override
    public void entityAdded(Entity entity){
        fetchEntities();
    }
    
    @Override
    public void entityRemoved(Entity entity){
        fetchEntities();
    }
    
    private void fetchEntities(){
		cameraEntity = (CameraEntity) getEntity(Family.all(CameraComponent.class)      .get());
		smewEntity   = (SmewEntity)   getEntity(Family.all(SmewMovementComponent.class).get());
		mapEntity    = (MapEntity)    getEntity(Family.all(MapComponent.class)         .get());
	}
    
    private boolean ensureEntities(){
        return (cameraEntity != null && smewEntity != null && mapEntity != null);
    }
    
    private Entity getEntity(Family family){
        ImmutableArray<Entity> array = getEngine().getEntitiesFor(family);
        if (array.size()>0){
            return array.get(0);
        }
        return null;
    }

    @Override
    public void update(float deltaTime) {
        if (!ensureEntities()){
            fetchEntities();
            return;
        }
        
        CameraComponent cameraComponent        = cm.get(cameraEntity);
        PhysicsComponent smewPhysicsComponent  = pm.get(smewEntity);
        MapComponent mapComponent              = mm.get(mapEntity);
        
        TileMapManager map = mapComponent.map;
        ArrayList<RoomBound> roomBounds = map.roomBounds;
        OrthographicCamera camera = cameraComponent.camera;
        
        Vector2 smewPos = smewPhysicsComponent.getCentre();
        Vector2 cameraPos = cameraComponent.getPosition();
        
        if (currentBound != null){
            if (currentBound.collide(smewPos)){
                cameraComponent.animatePositionTo(currentBound.limitPadded(smewPos), 0.3f);
            }
            else {
                currentBound = null;
            }
        }
        if (currentBound == null){
            for (RoomBound bound : roomBounds){
                if (bound.collide(smewPos)){
                    currentBound = bound;
                    cameraComponent.animatePositionTo(currentBound.limitPadded(smewPos), 0.3f);
                    break;
                }
            }
        }
        if (currentBound == null){
            cameraComponent.animatePositionTo(smewPos, 0.3f);
        }
        
        cameraComponent.updateAnimation(deltaTime);
    }
}