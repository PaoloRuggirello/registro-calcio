package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.model.general.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;

@Component
@Slf4j
public class TournamentHandler {

    public List<User> sortUsers(List<User> users) {
        log.info("Sorting list of users: {}", users);
        if (isEmpty(users)) {
            return emptyList();
        }
        users.sort(new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                int result = u1.getName().compareTo(u2.getName());
                if (result == 0) {
                    result = u1.getSurname().compareTo(u2.getSurname());
                }
                return result;
            }
        });
        log.debug("Sorted list of users: {}", users);
        return users;
    }
}
