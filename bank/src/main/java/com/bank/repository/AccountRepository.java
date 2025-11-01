package com.bank.repository;

import com.bank.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class AccountRepository {
    private EntityManagerFactory emf;

    public AccountRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<Account> findByEmail(String email) {
        EntityManager em = getEntityManager();
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.email = :email", Account.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }

    public boolean existsByAccountId(Long accountId) {
        EntityManager em = getEntityManager();
        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Account a WHERE a.accountId = :id", Long.class)
                .setParameter("id", accountId)
                .getSingleResult();
        return count > 0;
    }

    public Optional<Account> findByUserId(Long userId) {
        EntityManager em = getEntityManager();
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.user.id = :userId", Account.class);
        query.setParameter("userId", userId);
        return query.getResultStream().findFirst();
    }

    public Optional<Account> findByAccountId(Long accountId) {
        EntityManager em = getEntityManager();
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a LEFT JOIN FETCH a.cards WHERE a.accountId = :id", Account.class);
        query.setParameter("id", accountId);
        return query.getResultStream().findFirst();
    }

    public List<Account> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }

    public Optional<Account> findByIdWithCards(Long id) {
        EntityManager em = getEntityManager();
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a LEFT JOIN FETCH a.cards WHERE a.accountId = :id", Account.class);
        query.setParameter("id", id);
        return query.getResultStream().findFirst();
    }

    public Optional<Account> findByVerificationToken(String token) {
        EntityManager em = getEntityManager();
        TypedQuery<Account> query = em.createQuery(
                "SELECT a FROM Account a WHERE a.verificationToken = :token", Account.class);
        query.setParameter("token", token);
        return query.getResultStream().findFirst();
    }

    public Account save(Account account) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (account.getAccountId() == null) {
                em.persist(account);
            } else {
                account = em.merge(account);
            }
            em.getTransaction().commit();
            return account;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Account account) {
        EntityManager em = getEntityManager();
        Account acc = em.find(Account.class, account.getAccountId());
        if (acc != null) {
            em.remove(acc);
        }
    }

}
