package com.example.uaipapo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.uaipapo.adapter.SearchUserRecyclerAdapter;
import com.example.uaipapo.model.ChatroomModel;
import com.example.uaipapo.model.UserModel;
import com.example.uaipapo.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchUserActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String TAG = "SearchUserActivity";

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;
    List<String> phoneContactsNumbers = new ArrayList<>();
    List<UserModel> appUserContacts = new ArrayList<>(); // Lista para armazenar contatos que são usuários do app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.seach_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchInput.requestFocus();

        backButton.setOnClickListener(v -> onBackPressed());

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString().trim();
            if (searchTerm.isEmpty()) {
                // Se a busca estiver vazia, mostre os contatos do app
                setupSearchRecyclerView(""); // Passar string vazia para indicar que não é busca por termo
            } else if (searchTerm.length() < 3) {
                searchInput.setError("Invalid Username");
                return;
            } else {
                setupSearchRecyclerView(searchTerm.toLowerCase());
            }
        });

        // Adiciona um TextWatcher para limpar a busca e recarregar os contatos quando o texto for apagado
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    setupSearchRecyclerView(""); // Recarrega contatos do app
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Solicitar permissão e carregar contatos
        requestContactsPermission();
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // Permissão já concedida, carregar contatos
            loadPhoneContactsAndThenAppUsers();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPhoneContactsAndThenAppUsers();
            } else {
                Toast.makeText(this, "Permissão de contatos negada. Não é possível mostrar contatos do telefone.", Toast.LENGTH_LONG).show();
                // Mesmo sem permissão, podemos tentar carregar a busca normal se o usuário digitar algo
                setupSearchRecyclerView(""); // Para garantir que a lista não fique vazia inicialmente se não houver busca
            }
        }
    }

    private void loadPhoneContactsAndThenAppUsers() {
        getPhoneContacts();
        if (!phoneContactsNumbers.isEmpty()) {
            fetchAppUsersFromContacts();
        } else {
            Log.d(TAG, "Nenhum contato encontrado no telefone.");
            // Mesmo sem contatos no telefone, configurar o RecyclerView para buscas futuras
            setupSearchRecyclerView(""); // Configura inicialmente com lista vazia ou resultado de busca vazia
        }
    }


    private void getPhoneContacts() {
        phoneContactsNumbers.clear();
        Set<String> uniqueNormalizedNumbers = new HashSet<>(); // Para evitar duplicados

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (phoneNumberColumnIndex == -1) {
                Log.e(TAG, "Coluna Phone.NUMBER não encontrada.");
                cursor.close();
                return;
            }
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(phoneNumberColumnIndex);
                if (phoneNumber != null) {
                    String normalizedNumber = phoneNumber.replaceAll("[^0-9+]", "");
                    if (!normalizedNumber.isEmpty() && uniqueNormalizedNumbers.add(normalizedNumber)) {
                        phoneContactsNumbers.add(normalizedNumber);
                        Log.d(TAG, "Contato encontrado: " + normalizedNumber);
                    }
                }
            }
            cursor.close();
        } else {
            Log.d(TAG, "Nenhum contato encontrado no cursor.");
        }
        Log.d(TAG, "Total de contatos do telefone (normalizados e únicos): " + phoneContactsNumbers.size());
    }


    private void fetchAppUsersFromContacts() {
        if (phoneContactsNumbers.isEmpty()) {
            Log.d(TAG, "fetchAppUsersFromContacts: Lista de números de telefone vazia.");
            setupSearchRecyclerView(""); // Configura com lista vazia de contatos do app
            return;
        }
        appUserContacts.clear();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < phoneContactsNumbers.size(); i += 10) {
            List<String> sublist = phoneContactsNumbers.subList(i, Math.min(i + 10, phoneContactsNumbers.size()));
            if (!sublist.isEmpty()) {
                Query usersQuery = FirebaseUtil.allUserCollectionReference()
                        .whereIn("phone", sublist); // Assumindo que você tem um campo 'phone' no seu UserModel
                tasks.add(usersQuery.get());
            }
        }

        if (tasks.isEmpty()) {
            Log.d(TAG, "fetchAppUsersFromContacts: Nenhuma task de query criada.");
            setupSearchRecyclerView("");
            return;
        }


        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            Set<String> foundUserIds = new HashSet<>(); // Para evitar duplicados se um contato tiver múltiplos números
            for (Object result : results) {
                QuerySnapshot querySnapshot = (QuerySnapshot) result;
                for (QueryDocumentSnapshot document : querySnapshot) {
                    UserModel user = document.toObject(UserModel.class);
                    // Evitar adicionar o próprio usuário e duplicados
                    if (!user.getUserId().equals(FirebaseUtil.currentUserId()) && foundUserIds.add(user.getUserId())) {
                        appUserContacts.add(user);
                    }
                }
            }
            Log.d(TAG, "Contatos do app encontrados: " + appUserContacts.size());
            setupSearchRecyclerView(""); // "" indica para mostrar contatos do app
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro ao buscar usuários do app: ", e);
            setupSearchRecyclerView(""); // Mesmo em caso de falha, configurar o recycler
        });
    }


    void setupSearchRecyclerView(String searchTerm) {
        FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .get().addOnCompleteListener(task -> {
                    List<String> existingChatContactIds = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (ChatroomModel chatroom : task.getResult().toObjects(ChatroomModel.class)) {
                            if (!chatroom.isGroupChat()) { // Apenas chats individuais
                                for (String userId : chatroom.getUserIds()) {
                                    if (!userId.equals(FirebaseUtil.currentUserId())) {
                                        existingChatContactIds.add(userId);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Erro ao buscar chatrooms existentes: ", task.getException());
                    }

                    if (searchTerm == null || searchTerm.isEmpty()) {
                        if (adapter != null) {
                            adapter.stopListening(); // Parar o listener anterior se houver
                        }
                        if (appUserContacts.isEmpty() && phoneContactsNumbers.isEmpty() && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Nenhum contato do telefone é usuário do app ou não há contatos no telefone.");
                            Query emptyQuery = FirebaseUtil.allUserCollectionReference().whereEqualTo("userId", "nonexistentuser"); // Query que não retorna nada
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(emptyQuery, UserModel.class).build();
                            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext(), existingChatContactIds);

                        } else if (appUserContacts.isEmpty() && !phoneContactsNumbers.isEmpty()) {
                            Log.d(TAG, "Contatos do telefone encontrados, mas nenhum é usuário do app.");
                            Query emptyQuery = FirebaseUtil.allUserCollectionReference().whereEqualTo("userId", "nonexistentuser");
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(emptyQuery, UserModel.class).build();
                            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext(), existingChatContactIds);
                        }
                        else if (!appUserContacts.isEmpty()){
                            List<String> appUserContactIds = new ArrayList<>();
                            for(UserModel user : appUserContacts){
                                appUserContactIds.add(user.getUserId());
                            }
                            Query contactsQuery = FirebaseUtil.allUserCollectionReference()
                                    .whereIn("userId", appUserContactIds.isEmpty() ? java.util.Collections.singletonList("placeholder") : appUserContactIds); // whereIn não pode receber lista vazia

                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(contactsQuery, UserModel.class).build();
                            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext(), existingChatContactIds);
                        } else {
                            Query query = FirebaseUtil.allUserCollectionReference()
                                    .whereEqualTo("searchUsername", "empty_placeholder_for_no_search_term_if_needed"); // Para não buscar nada inicialmente
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(query, UserModel.class).build();
                            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext(), existingChatContactIds);
                        }

                    } else {
                        if (adapter != null) {
                            adapter.stopListening();
                        }
                        Query query = FirebaseUtil.allUserCollectionReference()
                                .whereGreaterThanOrEqualTo("searchUsername", searchTerm)
                                .whereLessThanOrEqualTo("searchUsername", searchTerm + '\uf8ff')
                                .whereNotEqualTo("userId", FirebaseUtil.currentUserId()); // Não mostrar o próprio usuário na busca

                        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                .setQuery(query, UserModel.class).build();
                        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext(), existingChatContactIds);
                    }

                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchInput.getText().toString().trim().isEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if(phoneContactsNumbers.isEmpty()){
                    loadPhoneContactsAndThenAppUsers();
                } else if (appUserContacts.isEmpty() && !phoneContactsNumbers.isEmpty()){
                    fetchAppUsersFromContacts();
                }
                else {
                    setupSearchRecyclerView("");
                }
            } else {
                setupSearchRecyclerView("");
            }
        } else if (adapter != null) {
            adapter.startListening();
        }
    }
}
