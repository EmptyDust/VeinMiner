package org.cat.cat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MineListener implements Listener {

    private static final ArrayList<Material> ores = new ArrayList<>();
    private static final ArrayList<Material> crops = new ArrayList<>();
    private static final ArrayList<Material> seeds = new ArrayList<>();
    private static final Set<UUID> openedPlayerUUID = new HashSet<>();
    private static final Set<UUID> withoutSneakPlayerUUID = new HashSet<>();
    private static final Queue<Block> blockQueue = new LinkedList<>();
    private static final Set<Block> mineBlock = new HashSet<>();
    private final int maxChainMine = 64;

    public static void addOpenedPlayerUUID(UUID uuid){openedPlayerUUID.add(uuid);}
    public static void removeOpenedPlayerUUID(UUID uuid){openedPlayerUUID.remove(uuid);}
    public static boolean containOpenedPlayerUUID(UUID uuid){return openedPlayerUUID.contains(uuid);}
    public static void addSneakPlayerUUID(UUID uuid){withoutSneakPlayerUUID.remove(uuid);}
    public static void removeSneakPlayerUUID(UUID uuid){withoutSneakPlayerUUID.add(uuid);}
    public static boolean containSneakPlayerUUID(UUID uuid){return !withoutSneakPlayerUUID.contains(uuid);}

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ItemStack mainHand = p.getInventory().getItemInMainHand();

        if (!p.isSneaking()&&containSneakPlayerUUID(uuid)) return;
        if (!containOpenedPlayerUUID(uuid)) return;

        for (Material t:Tag.ITEMS_AXES.getValues()) {
            if (mainHand.getType().equals(t)){
                for (Material m:Tag.LOGS.getValues()) {
                    if (b.getType().equals(m)) {
                        mine(p,b,maxChainMine);
                        break;
                    }
                }
                return;
            }
        }

        for (Material t:Tag.ITEMS_PICKAXES.getValues()) {
            if (mainHand.getType().equals(t)) {
                for (Material m:ores){
                    if (b.getType().equals(m)) {
                        mine(p,b,maxChainMine);
                        break;
                    }
                }
                return;
            }
        }
/*
        cropList();
        for (Material t:Tag.ITEMS_HOES.getValues()){
            if (mainHand.getType().equals(t)){
                for (Material m:crops){
                    if (b.getType().equals(m)){
                        plant(p,b,maxChainMine);
                        break;
                    }
                }
                return;
            }
        }
*/
    }

/*
    public void plant(Player p, Block b,int n){
        if (n<1) return;
        String name = b.getType().name();
        b.breakNaturally();
        for (int x=1;x>-2;x--){
            for (int y=1;y>-2;y--) {
                for (int z=1;z>-2;z--){
                    if (!(x==0&&y==0&&z==0)){
                        Location loc = b.getLocation();
                        Block b1 = loc.add(x,y,z).getBlock();
                        for (Material m:crops){
                            if (b1.getType().name().equalsIgnoreCase(name)) {
                                plant(p,b1,n-1);
                            }
                        }
                    }
                }
            }
        }
    }
*/
    private void mine(Player player, Block block, int maxMine) {
        if (maxMine < 1) return;
        String blockTypeName = block.getType().name();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (getDurability(mainHand) == 1)
            return;

        int i = 0;

        blockQueue.offer(block);
        mineBlock.add(block);
        while (!blockQueue.isEmpty()) {
            Block nextBlock = blockQueue.poll();
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Location loc = nextBlock.getLocation();
                        Block nearBlock = loc.add(x, y, z).getBlock();
                        String nearBlockTypeName = nearBlock.getType().name();
                        if (nearBlockTypeName.equalsIgnoreCase(blockTypeName) && !mineBlock.contains(nearBlock)) {
                            blockQueue.offer(nearBlock);
                            mineBlock.add(nearBlock);
                            Bukkit.getLogger().info(nearBlock.toString());
                            i++;
                            if (i >= maxMine) {
                                blockQueue.clear();
                                destroyBlocks(player);
                                return;
                            }
                        }
                    }
                }
            }
        }
        destroyBlocks(player);
    }

    private void destroyBlocks(Player player){
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        int durability = getDurability(mainHand);
        for (Block block:mineBlock) {
            if (durability==1)break;
            damage(player);
            block.breakNaturally(mainHand);
            durability--;
        }
        mineBlock.clear();
    }

    public void damage(Player p){
        decrementDurability(p,1);
    }

    private void decrementDurability (Player p, int n) {
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int d = damageable.getDamage();
            damageable.setDamage(d+n);
            item.setItemMeta(damageable);
        }
    }
    public int getDurability(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable damageable) {
            return itemStack.getType().getMaxDurability() - damageable.getDamage();
        }
        return 0;
    }

    public static void oreList() {
        ores.add(Material.COAL_ORE);
        ores.add(Material.COPPER_ORE);
        ores.add(Material.DIAMOND_ORE);
        ores.add(Material.LAPIS_ORE);
        ores.add(Material.EMERALD_ORE);
        ores.add(Material.IRON_ORE);
        ores.add(Material.GOLD_ORE);
        ores.add(Material.REDSTONE_ORE);
        ores.add(Material.NETHER_GOLD_ORE);
        ores.add(Material.NETHER_QUARTZ_ORE);
        ores.add(Material.DEEPSLATE_COAL_ORE);
        ores.add(Material.DEEPSLATE_COPPER_ORE);
        ores.add(Material.DEEPSLATE_DIAMOND_ORE);
        ores.add(Material.DEEPSLATE_LAPIS_ORE);
        ores.add(Material.DEEPSLATE_EMERALD_ORE);
        ores.add(Material.DEEPSLATE_IRON_ORE);
        ores.add(Material.DEEPSLATE_GOLD_ORE);
        ores.add(Material.DEEPSLATE_REDSTONE_ORE);
    }

    public static void cropList(){
        crops.add(Material.WHEAT);
    }
}
