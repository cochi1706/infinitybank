package com.mobile.infinitybank.repository;

import com.google.android.gms.tasks.Task;
import com.mobile.infinitybank.model.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository extends BaseRepository<User> {
    @Inject
    public UserRepository() {
        super("user");
    }

    public Task<Void> createUser(User user) {
        return db.collection(collectionName)
                .document(user.getId())
                .set(user);
    }

    public Task<Void> updateUser(User user) {
        return db.collection(collectionName)
                .document(user.getId())
                .set(user);
    }
}
