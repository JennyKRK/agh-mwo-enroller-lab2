package com.company.enroller.persistence;

import com.company.enroller.model.Participant;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

@Component("participantService")
public class ParticipantService {

	@Autowired
	PasswordEncoder passwordEncoder;

	DatabaseConnector connector;

	public ParticipantService() {
		connector = DatabaseConnector.getInstance();
	}

	public Collection<Participant> getAll() {
		return connector.getSession().createCriteria(Participant.class).list();
	}

	public Participant findByLogin(String login) {
		return connector.getSession().get(Participant.class, login);
	}

	public Participant add(Participant participant) {

		String hashedPassword = passwordEncoder.encode(participant.getPassword());
		participant.setPassword(hashedPassword);
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().save(participant);
		transaction.commit();
		return participant;
	}

	public void update(Participant participant) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().merge(participant);
		transaction.commit();
	}

	public void delete(Participant participant) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().delete(participant);
		transaction.commit();
	}

	public Collection<Participant> getAllParticipantsSorted(String sortBy, String sortOrder, String key) {
		String hql = "FROM Participant WHERE Login LIKE :key";


		if (sortBy.equals("login")){
			hql += " ORDER BY login";
		}
		if (sortOrder.equals("ASC")) {
			hql += " ASC";
		}
		if (sortOrder.equals("DESC")) {
			hql += " DESC";
		}

		Query query = connector.getSession().createQuery(hql, Participant.class);
		query.setParameter("key","%" + key + "%");
		return query.list();
	}



}
