package helpers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import java.util.function.Supplier;

/**
 * It's helper of non-transaction entityManager's create and close.
 */
@Singleton
public class NoTxJPA {

    private final JPAApi jpa;

    private ThreadLocal<EntityManager> emHolder = new ThreadLocal<>();

    @Inject
    public NoTxJPA(JPAApi jpa) {
        this.jpa = jpa;
    }
    /* TODO:
     This method is called in Service method, or controller when calling service method, or interceptor.
  */
    public <T> T withDefaultEm(Supplier<T> block) {
        EntityManager em = null;
        try {

            em = jpa.em("default");
            emHolder.set(em);
            T ret = block.get(); // in block, call jpaapi#em();
            return ret;
        } finally {
            if (em != null) {

                em.close();
            }
            emHolder.remove();

        }
    }

    public void withDefaultEm(Runnable block) {
        EntityManager em = null;
        try {

            em = jpa.em("default");
            emHolder.set(em);
            block.run();
        } finally {
            if (em != null) {

                em.close();
            }
            emHolder.remove();

        }
    }

    public EntityManager currentEm() {
//        if(emHolder.get() == null) {
//            throw new IllegalStateException("not set em in current thread");
//        }
//        return emHolder.get();
        return jpa.em("default");
    }



}
