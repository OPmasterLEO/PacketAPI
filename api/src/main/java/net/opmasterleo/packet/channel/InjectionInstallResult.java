package net.opmasterleo.packet.channel;

/**
 * Outcome of an ownership-aware PacketAPI pipeline install.
 */
public enum InjectionInstallResult {
    INSTALLED,
    REPLACED_OWN,
    ALREADY_OWN,
    CONFLICT_FOREIGN,
    NO_CHANNEL
}
