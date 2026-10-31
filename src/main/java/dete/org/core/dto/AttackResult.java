package dete.org.core.dto;

/**
 * <p>
 *     Esta classe foi feita apenas para transferência de dados
 *     para a UI, sem que a classe Entity saiba como vai ser
 *     aplicado na UI.
 * </p>
 * */
public record AttackResult(double damage, boolean critical) {}
