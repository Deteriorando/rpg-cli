package dete.org.core.entity;

@SuppressWarnings("UnusedReturnValue")
public interface EntityBehaviour {
    double attack(Entity target);
    double regeneration(double value);
}