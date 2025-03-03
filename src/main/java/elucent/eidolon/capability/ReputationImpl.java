package elucent.eidolon.capability;

import elucent.eidolon.common.spell.PrayerSpell;
import elucent.eidolon.registries.Spells;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class ReputationImpl implements IReputation, INBTSerializable<CompoundTag> {
    final Map<UUID, Map<ResourceLocation, ReputationEntry>> reputationMap = new HashMap<>();
    final Map<UUID, Map<ResourceLocation, Long>> prayerTimes = new HashMap<>();

    @Override
    public double getReputation(UUID player, ResourceLocation deity) {
        return getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry()).reputation;
    }

    @Override
    public void addReputation(UUID player, ResourceLocation deity, double amount) {
        ReputationEntry entry = getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry());
        if (entry.lock == null) entry.reputation += amount;
    }

    @Override
    public void subtractReputation(UUID player, ResourceLocation deity, double amount) {
        ReputationEntry entry = getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry());
        entry.reputation = Math.max(0, entry.reputation - amount);
    }

    @Override
    public void setReputation(UUID player, ResourceLocation deity, double amount) {
        ReputationEntry entry = getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry());
        if (entry.lock == null || amount < 0) {
            double prev = entry.reputation;
            entry.reputation = amount;
        }
    }

    @Override
    public boolean isLocked(UUID player, ResourceLocation deity) {
        return getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry()).lock != null;
    }

    @Override
    public boolean hasLock(UUID player, ResourceLocation deity, ResourceLocation lock) {
        ResourceLocation l = getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry()).lock;
        return l != null && l.equals(lock);
    }

    @Override
    public void lock(UUID player, ResourceLocation deity, ResourceLocation key) {
        getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry()).lock = key;
    }

    @Override
    public boolean unlock(UUID player, ResourceLocation deity, ResourceLocation key) {
        ReputationEntry entry = getReputationMap(player).computeIfAbsent(deity, (k) -> new ReputationEntry());
        if (entry.lock != null && entry.lock.equals(key)) {
            entry.lock = null;
            return true;
        }
        return false;
    }

    @Override
    public void pray(UUID player, PrayerSpell spell, long time) {
        getPrayerTimes().computeIfAbsent(player, (p) -> new HashMap<>()).put(spell.getRegistryName(), time);
    }

    @Override
    public boolean canPray(UUID player, PrayerSpell spell, long time) {
        Map<ResourceLocation, Long> times = getPrayerTimes().computeIfAbsent(player, (p) -> new HashMap<>());
        return !times.containsKey(spell.getRegistryName()) || times.get(spell.getRegistryName()) < time - spell.getCooldown();
    }

    @Override
    public Map<UUID, Map<ResourceLocation, Long>> getPrayerTimes() {
        return prayerTimes;
    }

    @Override
    public Map<UUID, Map<ResourceLocation, ReputationEntry>> getReputationMap() {
        return reputationMap;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag data = new CompoundTag();
        CompoundTag reps = new CompoundTag();
        for (Entry<UUID, Map<ResourceLocation, ReputationEntry>> e : getReputationMap().entrySet()) {
            CompoundTag tag = new CompoundTag();
            for (Entry<ResourceLocation, ReputationEntry> e2 : e.getValue().entrySet()) {
                CompoundTag entry = new CompoundTag();
                entry.putDouble("rep", e2.getValue().reputation);
                if (e2.getValue().lock != null) entry.putString("lock", e2.getValue().lock.toString());
                tag.put(e2.getKey().toString(), entry);
            }
            reps.put(e.getKey().toString(), tag);
        }
        CompoundTag times = new CompoundTag();
        for (Entry<UUID, Map<ResourceLocation, Long>> e : getPrayerTimes().entrySet()) {
            CompoundTag nbt = new CompoundTag();
            for (Entry<ResourceLocation, Long> e2 : e.getValue().entrySet())
                nbt.putLong(e2.getKey().toString(), e2.getValue());
            times.put(e.getKey().toString(), nbt);
        }
        data.put("reps", reps);
        data.put("times", times);
        return data;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getReputationMap().clear();
        if (nbt.contains("reps")) {
            CompoundTag reps = nbt.getCompound("reps");
            for (String uuidString : reps.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidString);
                CompoundTag tag = reps.getCompound(uuidString);
                for (String deity : tag.getAllKeys()) {
                    CompoundTag entry = tag.getCompound(deity);
                    setReputation(uuid, new ResourceLocation(deity), entry.getDouble("rep"));
                    if (entry.contains("lock"))
                        lock(uuid, new ResourceLocation(deity), new ResourceLocation(entry.getString("lock")));
                }
            }
        }
        if (nbt.contains("times")) {
            CompoundTag times = nbt.getCompound("times");
            for (String uuidString : times.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidString);
                CompoundTag spelltimes = times.getCompound(uuidString);
                for (String rl : spelltimes.getAllKeys()) {
                    if (Spells.find(new ResourceLocation(rl)) instanceof PrayerSpell prayerSpell)
                        pray(uuid, prayerSpell, spelltimes.getLong(rl));
                }
            }
        }
    }

}
