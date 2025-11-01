package com.bank.repository;

import com.bank.model.OtpTransaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class OtpTransactionRepository {

    private final EntityManagerFactory emf;

    public OtpTransactionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Tìm OtpTransaction theo transactionId
     */
    public Optional<OtpTransaction> findByTransaction_TransactionId(Long transactionId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<OtpTransaction> query = em.createQuery(
                    "SELECT o FROM OtpTransaction o WHERE o.transaction.transactionId = :transactionId",
                    OtpTransaction.class);
            query.setParameter("transactionId", transactionId);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm tất cả bản ghi
     */
    public List<OtpTransaction> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT o FROM OtpTransaction o", OtpTransaction.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lưu mới hoặc cập nhật OTP transaction
     */
    public void save(OtpTransaction otpTransaction) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            if (otpTransaction.getId() == null) {
                em.persist(otpTransaction);
            } else {
                em.merge(otpTransaction);
            }

            em.getTransaction().commit(); // ✅ phải commit để lưu xuống DB
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback(); // 🔄 rollback nếu lỗi
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa bản ghi theo ID
     */
    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();

            OtpTransaction otp = em.find(OtpTransaction.class, id);
            if (otp != null) {
                em.remove(otp);
            }

            em.getTransaction().commit(); // ✅ phải commit
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
