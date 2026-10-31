package dete.org.core.entity;

import dete.org.core.dto.AttackResult;

@SuppressWarnings("UnusedReturnValue")
public interface EntityBehaviour {
    AttackResult attack(Entity target);
    double regeneration(double value);
}