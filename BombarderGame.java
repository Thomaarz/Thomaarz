
import java.util.*;

public class BombarderGame extends AMiniGame {

    private int remainingTime = 60 * 3;
    
    private static final int LIFES = 3;

    public BombarderGame() {
        super("Dé à Coudre",
                MinigameType.DISQUALIFICATION,
                MinigameListener.PRE_START,
                "Survie aux boulez de feu !",
                "Survie aux boules de feu et ne tombe pas dans le vide pour rester en vie !");
    }

    @Override
    public ArrayList<PixelMap> getAllMaps() {
        return new ArrayList<>(Arrays.asList(
                new PixelMap("BOMBARDEMENT1", 0.5, 152, 0.5, 0, 0)
        ));
    }

    @Override
    public void onGamePreStart() {
        super.onGamePreStart();
        makeInvisiblePlayers();
    }

    @Override
    public void onGameStart() {
        super.onGameStart();
        
        this.getMinigame().getInstance().getPlayers().forEach(player -> {
            player.setHealthScale(LIFES * 2);
            player.setMaxHealth(LIFES * 2);
            player.setHealth(LIFES * 2);
        });

        this.registerTask(new BukkitRunnable() {
            @Override
            public void run() {
                remainingTime--;
                getMinigame().getInstance().updateScoreboard();

                if (remainingTime <= 0) {
                    this.cancel();
                    getMinigame().endMinigame(player -> player.getGameMode().equals(GameMode.SPECTATOR));
                }
            }
        }.runTaskTimer(PixelPerfectGame.getInstance(), 20, 20));
    }
    
    @Override
    public ArrayList<String> updateScoreboard(Player player) {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("§7╸ §rTemps: §a" + TimerUtils.getTimeCooldown(this.remainingTime));
        lines.add(null);

        return lines;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        EntityDamageEvent.DamageCause cause = event.getCause();

        // Not in world
        if (!entity.getWorld().equals(getMinigame().getPixelMap().getWorld())) {
            return;
        }

        // Lava
        if (cause == EntityDamageEvent.DamageCause.LAVA) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }
    }

    @EventHandler
    public void onENtityDamageByEntity(EntityDamageEventByEntity event) {
        Entity entity = event.getEntity();

        // Not in world
        if (!entity.getWorld().equals(getMinigame().getPixelMap().getWorld())) {
            return;
        }

        if (!(entity instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof Fireball)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) entity;
        removeLife(player);

    }

    public void removeLife(Player player) {
        if (player.getHealthScale() - 2 <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setHealthScale(player.getHealthScale() - 2);
            player.setMaxHealth(player.getMaxHealth() - 2);
        }

        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 3.0F, 0.5F);
        player.teleport(getMinigame().getPixelMap().getSpawn());
    }
}
