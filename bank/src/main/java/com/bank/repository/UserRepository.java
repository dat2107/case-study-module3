package com.bank.repository;

import com.bank.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class UserRepository {

    private final EntityManagerFactory emf;

    public UserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Tạo EntityManager mới mỗi lần thao tác.
     */
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Tìm user theo username (chính xác).
     */
    public Optional<User> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username",
                    User.class);
            query.setParameter("username", username);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra username đã tồn tại (không phân biệt hoa thường).
     */
    public boolean existsByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE LOWER(u.username) = LOWER(:username)",
                            Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy toàn bộ danh sách user (fetch luôn account nếu có).
     */
    public List<User> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.account",
                            User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm user có username chứa chuỗi (tìm kiếm gần đúng, không phân biệt hoa thường).
     */
    public List<User> findByUsernameContainingIgnoreCase(String username) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))",
                            User.class)
                    .setParameter("username", username)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lưu hoặc cập nhật user.
     */
    public void save(User user) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (user.getId() == null) {
                em.persist(user);
            } else {
                em.merge(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa user theo ID.
     */
    public void delete(User user) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, user.getId());
            if (u != null) {
                em.remove(u);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
