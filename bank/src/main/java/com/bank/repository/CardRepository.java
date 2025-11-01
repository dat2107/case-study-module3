package com.bank.repository;

import com.bank.model.Card;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class CardRepository {

    private final EntityManagerFactory emf;

    public CardRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** Kiểm tra account có thẻ hay chưa */
    public boolean existsByAccount_AccountId(Long accountId) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM Card c WHERE c.account.accountId = :accountId",
                            Long.class)
                    .setParameter("accountId", accountId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách thẻ theo accountId */
    public List<Card> findByAccount_AccountId(Long accountId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Card c WHERE c.account.accountId = :accountId",
                            Card.class)
                    .setParameter("accountId", accountId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Tìm thẻ theo số thẻ */
    public Optional<Card> findByCardNumber(String cardNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Card> query = em.createQuery(
                    "SELECT c FROM Card c WHERE c.cardNumber = :cardNumber", Card.class);
            query.setParameter("cardNumber", cardNumber);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    /** Tìm thẻ theo ID */
    public Optional<Card> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Card.class, id));
        } finally {
            em.close();
        }
    }

    /** Đếm số lượng thẻ thuộc 1 account */
    public int countByAccount_AccountId(Long accountId) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM Card c WHERE c.account.accountId = :accountId",
                            Long.class)
                    .setParameter("accountId", accountId)
                    .getSingleResult();
            return count.intValue();
        } finally {
            em.close();
        }
    }

    /** Lưu thẻ mới hoặc cập nhật thẻ cũ */
    public void save(Card card) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            if (card.getCardId() == null) {
                em.persist(card);
            } else {
                em.merge(card);
            }

            em.getTransaction().commit(); // ✅ bắt buộc để lưu
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // 🔄 rollback khi có lỗi
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Xóa thẻ theo id */
    public void delete(Card card) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            Card c = em.find(Card.class, card.getCardId());
            if (c != null) {
                em.remove(c); // ✅ phải remove entity thuộc context hiện tại
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Lấy toàn bộ thẻ */
    public List<Card> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Card c", Card.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
