package org.pg6100.workshop;


import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class UserEJB {

    @PersistenceContext
    private EntityManager em;


    public long createUser(String name, String surname, String address){

        UserEntity user = new UserEntity();
        user.setName(name);
        user.setSurname(surname);
        user.setAddress(address);

        em.persist(user);

        return user.getId();
    }

    public UserEntity getUser(long id){
        return em.find(UserEntity.class, id);
    }

    public void update(Long id, String name, String surname, String address){

        UserEntity user = getUser(id);
        user.setName(name);
        user.setSurname(surname);
        user.setAddress(address);
    }
}
