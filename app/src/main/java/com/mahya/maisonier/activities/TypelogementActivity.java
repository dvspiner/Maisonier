package com.mahya.maisonier.activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.TypeLogementAdapter;
import com.mahya.maisonier.entites.TypeLogement;
import com.mahya.maisonier.entites.TypeLogement_Table;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.mahya.maisonier.utils.CustomLoadingListItemCreator;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemSpanLookup;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class TypelogementActivity extends android.app.Fragment implements Paginate.Callbacks, CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {
    private static final int GRID_SPAN = 3;
    private static final String TAG = TypelogementActivity.class.getSimpleName();
    protected int threshold = 6;
    protected int totalPages;
    protected int itemsPerPage = 8;
    protected int initItem = 20;
    protected long networkDelay = 2000;
    protected boolean addLoadingRow = true;
    protected boolean customLoadingListItem = false;
    protected BaseActivity.Orientation orientation = BaseActivity.Orientation.VERTICAL;
    protected RecyclerView mRecyclerView;
    TypeLogementAdapter mAdapter;
    FrameLayout fab;
    ImageButton myfab_main_btn;
    Animation animation;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = getActivity();
    private TextView tvEmptyView;
    private boolean loading = false;
    private int page = 0;
    private Handler handler;
    private Paginate paginate;
    private Runnable fakeCallback = new Runnable() {
        @Override
        public void run() {
            page++;
            mAdapter.add(TypeLogement.getInitData(itemsPerPage));
            loading = false;

        }
    };
    private CoordinatorLayout activityTypelogement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_model1, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);
        activityTypelogement = (CoordinatorLayout) view.findViewById(R.id.activity_typelogement);

        TypeLogement.typelogs.clear();
        TypeLogement.typelogs = TypeLogement.findAll();
        getActivity().setTitle("Type de logement");
        totalPages = TypeLogement.typelogs.size() / itemsPerPage - 1;
        System.out.println(totalPages);

        initView(view);
        fab.startAnimation(animation);
        handler = new Handler();
        setupPagination();
    }


    private void initView(View view) {

        fab = (FrameLayout) view.findViewById(R.id.myfab_main);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_item);
        tvEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mRecyclerView.setFilterTouchesWhenObscured(true);
        myfab_main_btn = (ImageButton) view.findViewById(R.id.myfab_main_btn);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        if (new TypeLogement().findAll().isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }

    }

    public void add(final View view) {
        switch (view.getId()) {
            case R.id.myfab_main_btn:
                ajouter(view);
                break;
        }
    }

    @Override
    public void ajouter(final View view) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_type_de_logement_);
        // Initialisation du formulaire

        final EditText Libelle = (EditText) dialog.findViewById(R.id.Libelle);
        final EditText Code = (EditText) dialog.findViewById(R.id.Code);
        final EditText Description = (EditText) dialog.findViewById(R.id.Description);

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Libelle.getText().toString().trim().equals("")) {
                    Libelle.setError("Velliez remplir le libelle");
                    return;

                }
                if (Code.getText().toString().trim().equals("")) {
                    Code.setError("Velliez remplir le code");
                    return;

                }
                if (Description.getText().toString().trim().equals("")) {
                    Description.setError("Velliez remplir la description");
                    return;

                }
                TypeLogement typeLogement = new TypeLogement();
                typeLogement.setCode(Code.getText().toString().trim());
                typeLogement.setLibelle(Libelle.getText().toString().trim());
                typeLogement.setDescription(Description.getText().toString().trim());
                try {
                    typeLogement.save();
                    Snackbar.make(view, "le type de logement a été correctement crée", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mAdapter.addItem(typeLogement, 0);
                } catch (Exception e) {
                    Snackbar.make(view, "echec", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


                dialog.dismiss();
            }
        });

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Libelle.setText("");
                Code.setText("");
                Description.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        //FlowManager.destroy();
        // Delete.tables(TypeLogement.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void supprimer(final int id) {

        new AlertDialog.Builder(context)
                .setTitle("Avertissement")
                .setMessage("Voulez vous vraimment supprimer ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {

                            TypeLogement typeLogement = new TypeLogement();
                            typeLogement.setId(id);
                            typeLogement.delete();

                        } catch (Exception e) {

                        }

                        mAdapter.deleteItem(mAdapter.getSelectposition());

                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {

        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            //  actionMode = getActivity().getActionBar().startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);

        return true;
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void detail(final int id) {
        final TypeLogement typeLogement = SQLite.select().from(TypeLogement.class).where(TypeLogement_Table.id.eq(id)).querySingle();

        AlertDialog detail = new AlertDialog.Builder(context)
                .setMessage(Html.fromHtml("<b>" + "Code: " + "</b> ") + typeLogement.getCode() + "\n" + "\n " + Html.fromHtml("<b>" + "Description: " + "</b> ") + typeLogement.getDescription())
                .setIcon(R.drawable.ic_info_indigo_900_18dp)
                .setTitle("Detail " + typeLogement.getLibelle())
                .setNeutralButton("OK", null)
                .setCancelable(false)
                .create();
        detail.show();

    }

    @Override
    public void modifier(final int id) {

        final TypeLogement typeLogement = SQLite.select().from(TypeLogement.class).where(TypeLogement_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_type_de_logement_);
        // Initialisation du formulaire

        final EditText Libelle = (EditText) dialog.findViewById(R.id.Libelle);
        final EditText Code = (EditText) dialog.findViewById(R.id.Code);
        final EditText Description = (EditText) dialog.findViewById(R.id.Description);
        Libelle.setText(typeLogement.getLibelle());
        Code.setText(typeLogement.getCode());
        Description.setText(typeLogement.getDescription());
        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Libelle.getText().toString().trim().equals("")) {
                    Libelle.setError("Velliez remplir le libelle");
                    return;

                }
                if (Code.getText().toString().trim().equals("")) {
                    Code.setError("Velliez remplir le code");
                    return;

                }
                if (Description.getText().toString().trim().equals("")) {
                    Description.setError("Velliez remplir la description");
                    return;

                }
                try {

                    typeLogement.setId(typeLogement.getId());
                    typeLogement.setCode(Code.getText().toString().trim());
                    typeLogement.setLibelle(Libelle.getText().toString().trim());
                    typeLogement.setDescription(Description.getText().toString().trim());
                    typeLogement.save();
                    mAdapter.actualiser(TypeLogement.findAll());
                    System.out.println("good");
                } catch (Exception e) {
                    System.out.println("erroo");
                    System.out.println(e.getMessage());
                }


                dialog.dismiss();
            }
        });
        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Libelle.setText("");
                Code.setText("");
                Description.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    protected void setupPagination() {
        // If RecyclerView was recently bound, unbind
        if (paginate != null) {
            paginate.unbind();
        }
        handler.removeCallbacks(fakeCallback);
        mAdapter = new TypeLogementAdapter(getActivity(), (ArrayList<TypeLogement>) TypeLogement.findAll(), this);
        loading = false;
        page = 0;

        mAdapter = new TypeLogementAdapter(context, TypeLogement.getInitData(initItem), this);
        mRecyclerView.setAdapter(mAdapter);


        ((TypeLogementAdapter) mAdapter).setMode(Attributes.Mode.Single);
        paginate = Paginate.with(mRecyclerView, this)
                .setLoadingTriggerThreshold(threshold)
                .addLoadingListItem(addLoadingRow)
                .setLoadingListItemCreator(customLoadingListItem ? new CustomLoadingListItemCreator(mRecyclerView) : null)
                .setLoadingListItemSpanSizeLookup(new LoadingListItemSpanLookup() {
                    @Override
                    public int getSpanSize() {
                        return GRID_SPAN;
                    }
                })
                .build();


    }

    @Override
    public synchronized void onLoadMore() {
        Log.d("Paginate", "onLoadMore");
        loading = true;
        // Fake asynchronous loading that will generate page of random data after some delay
        handler.postDelayed(fakeCallback, networkDelay);
    }

    @Override
    public synchronized boolean isLoading() {
        return loading; // Return boolean weather data is already loading or not
    }

    @Override
    public boolean hasLoadedAllItems() {
        return page == totalPages; // If all pages are loaded return true
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.model, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onQueryTextChange(String query) {
        final List<TypeLogement> filteredModelList = filter(TypeLogement.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private List<TypeLogement> filter(List<TypeLogement> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<TypeLogement> filteredModelList = new ArrayList<>();
        for (TypeLogement model : models) {
            final String text = model.getLibelle().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


    private class ActionModeCallback implements android.support.v7.view.ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {

            mode.getMenuInflater().inflate(R.menu.menu_supp, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return false;
        }


        @Override
        public boolean onActionItemClicked(final android.support.v7.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_supp:
                    new AlertDialog.Builder(context)
                            .setTitle("Avertissement")
                            .setMessage("Voulez vous vraimment supprimer ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {

                                        mAdapter.removeItems(mAdapter.getSelectedItems());
                                        mode.finish();

                                    } catch (Exception e) {

                                    }


                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            mAdapter.clearSelection();
            actionMode = null;
        }
    }
}
