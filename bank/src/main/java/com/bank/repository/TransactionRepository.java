package com.bank.repository;

import com.bank.enums.TransactionStatus;
import com.bank.model.Card;
import com.bank.model.Transaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public class TransactionRepository {

    private final EntityManagerFactory emf;

    public TransactionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** ======================== TRUY VẤN (READ) ======================== **/

    public List<Transaction> findByStatus(TransactionStatus status, int page, int size) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.status = :status",
                    Transaction.class);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAllPaginated(int page, int size) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long countAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Transaction t", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByStatus(TransactionStatus status) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.status = :status", Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCard_Account_AccountIdOrToCard_Account_AccountId(
            Long fromAccountId, Long toAccountId, int page, int size) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.fromCard.account.accountId = :fromId " +
                            "OR t.toCard.account.accountId = :toId ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setParameter("fromId", fromAccountId);
            query.setParameter("toId", toAccountId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCard_CardIdOrToCard_CardId(
            Long fromCardId, Long toCardId, int page, int size) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.fromCard.cardId = :fromId " +
                            "OR t.toCard.cardId = :toId ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setParameter("fromId", fromCardId);
            query.setParameter("toId", toCardId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByAccount_AccountId(Long accountId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t " +
                            "WHERE (t.fromCard IS NOT NULL AND t.fromCard.account.accountId = :accountId) " +
                            "OR (t.toCard IS NOT NULL AND t.toCard.account.accountId = :accountId) " +
                            "ORDER BY t.createdAt DESC",
                    Transaction.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCardAndStatus(Card fromCard, TransactionStatus status) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM Transaction t WHERE t.fromCard = :fromCard AND t.status = :status",
                            Transaction.class)
                    .setParameter("fromCard", fromCard)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCard_CardIdAndStatusIn(Long cardId, List<TransactionStatus> statuses) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM Transaction t WHERE t.fromCard.cardId = :cardId AND t.status IN :statuses",
                            Transaction.class)
                    .setParameter("cardId", cardId)
                    .setParameter("statuses", statuses)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCard_Account_AccountIdOrToCard_Account_AccountId(
            Long fromAccountId, Long toAccountId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM Transaction t WHERE t.fromCard.account.accountId = :fromId " +
                                    "OR t.toCard.account.accountId = :toId",
                            Transaction.class)
                    .setParameter("fromId", fromAccountId)
                    .setParameter("toId", toAccountId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByFromCard_Account_AccountIdAndStatusAndCreatedAtBetween(
            Long accountId, TransactionStatus status,
            LocalDateTime start, LocalDateTime end) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.fromCard.account.accountId = :accountId " +
                            "AND t.status = :status AND t.createdAt BETWEEN :start AND :end",
                    Transaction.class);
            query.setParameter("accountId", accountId);
            query.setParameter("status", status);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /** ======================== GHI DỮ LIỆU (WRITE) ======================== **/

    public void save(Transaction transaction) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            if (transaction.getTransactionId() == null) {
                em.persist(transaction);
            } else {
                em.merge(transaction);
            }

            em.getTransaction().commit(); // ✅ bắt buộc để lưu xuống DB
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // 🔄 rollback khi có lỗi
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            Transaction t = em.find(Transaction.class, id);
            if (t != null) {
                em.remove(t);
            }

            em.getTransaction().commit(); // ✅ bắt buộc
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Transaction> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT t FROM Transaction t", Transaction.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Transaction> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Transaction.class, id));
        } finally {
            em.close();
        }
    }
}
