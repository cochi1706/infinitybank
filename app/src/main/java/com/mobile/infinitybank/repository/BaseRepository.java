package com.mobile.infinitybank.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import javax.inject.Singleton;

@Singleton
public class BaseRepository<T> {

    protected final String collectionName;
    protected FirebaseFirestore db;

    public BaseRepository(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public Task<DocumentReference> create(T item) {
        return db.collection(collectionName).add(item);
    }

    public Task<Void> update(String id, T item) {
        return db.collection(collectionName).document(id).set(item);
    }

    public Task<Void> remove(String id) {
        return db.collection(collectionName).document(id).delete();
    }

    public Task<QuerySnapshot> findAll() {
        return db.collection(collectionName).get();
    }

    public Task<DocumentSnapshot> find(String id) {
        return db.collection(collectionName).document(id).get();
    }

    public Task<QuerySnapshot> findByField(String field, String value) {
        return db.collection(collectionName).whereEqualTo(field, value).get();
    }

    public Task<DocumentSnapshot> findFirstByField(String field, String value) {
        return db.collection(collectionName).whereEqualTo(field, value).limit(1).get().continueWith(task -> {
            QuerySnapshot querySnapshot = task.getResult();
            if (querySnapshot.isEmpty()) {
                return null;
            } else {
                return querySnapshot.getDocuments().get(0);
            }
        });
    }
}
