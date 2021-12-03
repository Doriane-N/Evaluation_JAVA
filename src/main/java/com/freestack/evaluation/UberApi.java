package com.freestack.evaluation;

import com.freestack.evaluation.models.Booking;
import com.freestack.evaluation.models.UberDriver;
import com.freestack.evaluation.models.UberUser;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.System.out;

public class UberApi {

    public static void enrollUser(UberUser uberUser) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(uberUser);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static void enrollDriver(UberDriver uberDriver) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(uberDriver);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static Booking bookOneDriver(UberUser uberUser) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            Booking newBooking = new Booking();
            newBooking.setUser(uberUser);
            newBooking.setStartOfTheBooking(LocalDateTime.now());

            Query queryAllAvailableDrivers = em.createQuery("select d from UberDriver d where d.available = true");
            List<UberDriver> availableDrivers = queryAllAvailableDrivers.getResultList();

            if (availableDrivers.isEmpty()) {
                newBooking = null;
                out.println("Il n'y a plus de chauffeurs disponibles, veuillez réessayer plus tard.");
            } else {
                newBooking.setDriver(availableDrivers.get(0));
                newBooking.getDriver().setAvailable(false);
                em.persist(newBooking);
            }
            em.getTransaction().commit();

            return newBooking;

        } finally {
            em.close();
        }
    }

    public static Booking finishBooking(Booking booking1) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            booking1.setEndOfTheBooking(LocalDateTime.now());
            booking1.getDriver().setAvailable(true);

            em.merge(booking1);
            em.merge(booking1.getDriver());
            em.getTransaction().commit();

            return booking1;

        } finally {
            em.close();
        }
    }

    public static Booking evaluateDriver(Booking booking1, int evaluationOfTheUser) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            booking1.setEvaluation(evaluationOfTheUser);

            em.merge(booking1);
            em.getTransaction().commit();

            return booking1;

        } finally {
            em.close();
        }
    }


    public static List<Booking> listDriverBookings(UberDriver uberDriver) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            Query queryBooking = em.createQuery("select b from Booking b where b.driver.id = :id");
            queryBooking.setParameter("id", uberDriver.getId());
            List<Booking> bookings = queryBooking.getResultList();

            return bookings;

        } finally {
            em.close();
        }
    }

    public static List<Booking> listUnfinishedBookings() {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            Query queryUnfinishedBooking = em.createQuery("select b from Booking b where b.endOfTheBooking = null");
            List<Booking> unfinishedBookings = queryUnfinishedBooking.getResultList();

            return unfinishedBookings;

        } finally {
            em.close();
        }
    }

    public static float meanScore(UberDriver uberDriver) {
        EntityManager em = EntityManagerFactorySingleton.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();

            Query queryEval = em.createQuery("Select AVG(b.evaluation) from Booking b where b.driver.id = :id");
            queryEval.setParameter("id", uberDriver.getId());
            Double meanScore = (Double) queryEval.getSingleResult();

            float meanScoreF = meanScore.floatValue();

            return meanScoreF;

        } finally {
            em.close();
        }
    }
}

