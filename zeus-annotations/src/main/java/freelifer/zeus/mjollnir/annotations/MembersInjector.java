package freelifer.zeus.mjollnir.annotations;

/**
 * @author zhukun on 2017/6/2.
 */
public interface MembersInjector<T> {

    /**
     * Injects dependencies into the fields and methods of {@code instance}. Ignores the presence or
     * absence of an injectable constructor.
     * <p>
     * <p>Whenever the object graph creates an instance, it performs this injection automatically
     * (after first performing constructor injection), so if you're able to let the object graph
     * create all your objects for you, you'll never need to use this method.
     *
     * @param instance into which members are to be injected
     * @throws NullPointerException if {@code instance} is {@code null}
     */
    void injectMembers(T instance);
}
