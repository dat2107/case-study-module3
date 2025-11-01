package com.bank.repository;

import com.bank.model.UserLevel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class UserLevelRepository {

    private final EntityManagerFactory emf;

    public UserLevelRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** Tạo EntityManager mới mỗi lần thao tác */
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<UserLevel> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(UserLevel.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<UserLevel> findByLevelName(String levelName) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<UserLevel> query = em.createQuery(
                    "SELECT u FROM UserLevel u WHERE u.levelName = :levelName",
                    UserLevel.class);
            query.setParameter("levelName", levelName);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    public List<UserLevel> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM UserLevel u", UserLevel.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public UserLevel save(UserLevel userLevel) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            UserLevel result;
            if (userLevel.getId() == null) {
                em.persist(userLevel);
                result = userLevel;
            } else {
                result = em.merge(userLevel);
            }
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            UserLevel level = em.find(UserLevel.class, id);
            if (level != null)
                em.remove(level);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
