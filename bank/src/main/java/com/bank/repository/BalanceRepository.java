package com.bank.repository;

import com.bank.model.Balance;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class BalanceRepository {

    private final EntityManagerFactory emf;

    public BalanceRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** Tạo EntityManager mới mỗi lần thao tác */
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<Balance> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Balance.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Balance> findByAccount_AccountId(Long accountId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Balance> query = em.createQuery(
                    "SELECT b FROM Balance b WHERE b.account.accountId = :accountId",
                    Balance.class);
            query.setParameter("accountId", accountId);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    public List<Balance> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT b FROM Balance b", Balance.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Balance save(Balance balance) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Balance result;
            if (balance.getBalanceId() == null) {
                em.persist(balance);
                result = balance;
            } else {
                result = em.merge(balance);
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
            Balance b = em.find(Balance.class, id);
            if (b != null) em.remove(b);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
