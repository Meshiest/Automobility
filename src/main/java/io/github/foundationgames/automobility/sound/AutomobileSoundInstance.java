package io.github.foundationgames.automobility.sound;

import io.github.foundationgames.automobility.entity.AutomobileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public abstract class AutomobileSoundInstance extends MovingSoundInstance {
    private final MinecraftClient client;
    private final AutomobileEntity automobile;

    private double lastDistance;

    private int fade = 0;
    private boolean die = false;

    public AutomobileSoundInstance(SoundEvent sound, MinecraftClient client, AutomobileEntity automobile) {
        super(sound, SoundCategory.AMBIENT);
        this.client = client;
        this.automobile = automobile;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    protected abstract boolean canPlay(AutomobileEntity automobile);

    @Override
    public void tick() {
        var player = this.client.player;
        if (automobile.isRemoved() || player == null) {
            this.setDone();
            return;
        } else if (!this.canPlay(automobile)) {
            this.die = true;
        }

        if (this.die) {
            if (this.fade > 0) this.fade--;
            else if (this.fade == 0) {
                this.setDone();
                return;
            }
        } else if (this.fade < 3) {
            this.fade++;
        }
        this.volume = (float)fade / 3;

        this.x = this.automobile.getX();
        this.y = this.automobile.getY();
        this.z = this.automobile.getZ();

        this.pitch = (float) (Math.pow(4, (this.automobile.getHSpeed() - 0.9)) + 0.32);

        if (player.getVehicle() != this.automobile) {
            double distance = this.automobile.getPos().subtract(player.getPos()).length();
            this.pitch += (0.36 * (lastDistance - distance));

            this.lastDistance = distance;
        } else {
            this.lastDistance = 0;
        }
    }

    public static class EngineSound extends AutomobileSoundInstance {
        public EngineSound(MinecraftClient client, AutomobileEntity automobile) {
            super(automobile.getEngine().sound(), client, automobile);
        }

        @Override
        protected boolean canPlay(AutomobileEntity automobile) {
            return automobile.engineRunning() || automobile.getBoostTimer() > 0;
        }
    }
}
