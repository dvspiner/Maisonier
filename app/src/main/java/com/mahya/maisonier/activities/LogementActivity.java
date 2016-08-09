package com.mahya.maisonier.activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.transition.ChangeTransform;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.LogementAdapter;
import com.mahya.maisonier.entites.Batiment;
import com.mahya.maisonier.entites.Logement;
import com.mahya.maisonier.entites.TypeLogement;
import com.mahya.maisonier.entites.TypeLogement_Table;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.mahya.maisonier.utils.CustomLoadingListItemCreator;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemSpanLookup;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LogementActivity extends BaseActivity implements Paginate.Callbacks, CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {
    private static final int GRID_SPAN = 3;
    private static final String TAG = LogementActivity.class.getSimpleName();
    protected RecyclerView mRecyclerView;
    DatePicker datePicker;
    Button changeDate;
    int month;
    LogementAdapter mAdapter;
    FrameLayout fab;
    ImageButton myfab_main_btn;
    Animation animation;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = this;
    private TextView tvEmptyView;
    private boolean loading = false;
    private int page = 0;
    private Handler handler;
    private Paginate paginate;
    private Runnable fakeCallback = new Runnable() {
        @Override
        public void run() {
            page++;
            mAdapter.add(Logement.getInitData(1));
            loading = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        super.setContentView(R.layout.activity_model1);
        Logement.logements.clear();
        Logement.logements = Logement.findAll();
        setTitle(context.getString(R.string.Logement));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        fab.startAnimation(animation);
        handler = new Handler();
        setupPagination();
    }

    private void initView() {

        fab = (FrameLayout) findViewById(R.id.myfab_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_item);
        tvEmptyView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView.setFilterTouchesWhenObscured(true);
        myfab_main_btn = (ImageButton) findViewById(R.id.myfab_main_btn);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        if (Logement.findAll().isEmpty()) {
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
        dialog.setContentView(R.layout.add_logement);
        // Initialisation du formulaire

        final Spinner type = (Spinner) dialog.findViewById(R.id.TypeDeLogement);
        final Spinner batiment = (Spinner) dialog.findViewById(R.id.Batiment);
        final EditText ref = (EditText) dialog.findViewById(R.id.Reference);
        final EditText prixMin = (EditText) dialog.findViewById(R.id.PrixMin);
        final EditText priwMax = (EditText) dialog.findViewById(R.id.PrixMax);
        final EditText desc = (EditText) dialog.findViewById(R.id.Description);
        final EditText date = (EditText) dialog.findViewById(R.id.date);
        final Button selectDate = (Button) dialog.findViewById(R.id.dateSelect);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                date.setText(currentDate());
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        date.setText(currentDate());
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        date.setText(currentDate());
                        dialog1.dismiss();
                    }
                });
            }
        });
        ArrayAdapter<TypeLogement> adapter1 =
                new ArrayAdapter<TypeLogement>(this, R.layout.spinner_item, TypeLogement.findAll());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter1.add(new TypeLogement(" jdsjdfhd"));
        type.setAdapter(adapter1);

        ArrayAdapter<Batiment> adapter2 =
                new ArrayAdapter<Batiment>(this, R.layout.spinner_item, Batiment.findAll());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        batiment.setAdapter(adapter2);

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ref.getText().toString().trim().equals("")) {
                    ref.setError("Velliez remplir le nom");
                    return;

                }
                if (prixMin.getText().toString().trim().equals("")) {
                    prixMin.setError("Velliez remplir le prix min");
                    return;

                }
                if (priwMax.getText().toString().trim().equals("")) {
                    priwMax.setError("Velliez remplir le prix max");
                    return;

                }
                if (desc.getText().toString().trim().equals("")) {
                    desc.setError("Velliez remplir la description");
                    return;

                }
                if (batiment.getSelectedItem().toString().trim().equals("")) {
                    // bailleur.setEr("Velliez remplir le code");
                    return;

                }

                DateFormat   formatter = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    System.out.println(formatter.parse(date.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    Logement logement = new Logement();
                    logement.setReference(ref.getText().toString().trim());
                    logement.setPrixMax(Double.parseDouble(priwMax.getText().toString().trim()));
                    logement.setPrixMin(Double.parseDouble(prixMin.getText().toString().trim()));
                    logement.setDescription(desc.getText().toString().trim());
                    logement.setDatecreation(formatter.parse(date.getText().toString()));

                    logement.assoBatiment((Batiment) batiment.getSelectedItem());
                    logement.assoTypeLogement((TypeLogement) type.getSelectedItem());

                    logement.save();
                    Snackbar.make(view, "la logement a été correctement crée", Snackbar.LENGTH_LONG)

                            .setAction("Action", null).show();
                    mAdapter.addItem(0, logement);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Logement.findAll().isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    tvEmptyView.setVisibility(View.VISIBLE);

                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    tvEmptyView.setVisibility(View.GONE);
                }


                dialog.dismiss();
            }
        });

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref.setText("");
                desc.setText("");
                priwMax.setText("");
                prixMin.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public String currentDate() {
        StringBuilder mcurrentDate = new StringBuilder();
        month = datePicker.getMonth() + 1;
        mcurrentDate.append(datePicker.getDayOfMonth() + "/" + month + "/" + datePicker.getYear());
        return mcurrentDate.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        //FlowManager.destroy();
        // Delete.tables(Logement.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void supprimer(final int id) {

        new AlertDialog.Builder(this)
                .setTitle("Avertissement")
                .setMessage("Voulez vous vraimment supprimer ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {

                            Logement logement = new Logement();
                            logement.setId(id);
                            logement.delete();

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
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);

        return true;
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void detail(final int id) {
        final Logement logement = SQLite.select().from(Logement.class).where(TypeLogement_Table.id.eq(id)).querySingle();

        AlertDialog detail = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml("<b>" + "Code: " + "</b> ") + logement.getDescription() + "\n" + "\n " + Html.fromHtml("<b>" + "Description: " + "</b> ") + logement.getDescription())
                .setIcon(R.drawable.ic_info_indigo_900_18dp)
                .setTitle("Detail " + logement.getDescription())
                .setNeutralButton("OK", null)
                .setCancelable(false)
                .create();
        detail.show();

    }

    @Override
    public void modifier(final int id) {

        final Logement logemen = SQLite.select().from(Logement.class).where(TypeLogement_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_logement);
        // Initialisation du formulaire

        final Spinner type = (Spinner) dialog.findViewById(R.id.TypeDeLogement);
        final Spinner batiment = (Spinner) dialog.findViewById(R.id.Batiment);
        final EditText ref = (EditText) dialog.findViewById(R.id.Reference);
        final EditText prixMin = (EditText) dialog.findViewById(R.id.PrixMin);
        final EditText priwMax = (EditText) dialog.findViewById(R.id.PrixMax);
        final EditText desc = (EditText) dialog.findViewById(R.id.Description);

        ArrayAdapter<TypeLogement> adapter1 =
                new ArrayAdapter<TypeLogement>(this, R.layout.spinner_item, TypeLogement.findAll());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        type.setAdapter(adapter1);

        ArrayAdapter<Batiment> adapter2 =
                new ArrayAdapter<Batiment>(this, R.layout.spinner_item, Batiment.findAll());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        batiment.setAdapter(adapter2);

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ref.getText().toString().trim().equals("")) {
                    ref.setError("Velliez remplir le nom");
                    return;

                }
                if (prixMin.getText().toString().trim().equals("")) {
                    prixMin.setError("Velliez remplir le prix min");
                    return;

                }
                if (priwMax.getText().toString().trim().equals("")) {
                    priwMax.setError("Velliez remplir le prix max");
                    return;

                }
                if (desc.getText().toString().trim().equals("")) {
                    desc.setError("Velliez remplir la description");
                    return;

                }
                if (batiment.getSelectedItem().toString().trim().equals("")) {
                    // bailleur.setEr("Velliez remplir le code");
                    return;

                }

                Logement logement = new Logement();
                logement.setId(logemen.getId());
                logement.setReference(ref.getText().toString().trim());
                logement.setPrixMax(Double.parseDouble(priwMax.getText().toString().trim()));
                logement.setPrixMin(Double.parseDouble(prixMin.getText().toString().trim()));
                logement.setDescription(desc.getText().toString().trim());
                logement.assoBatiment((Batiment) batiment.getSelectedItem());
                logement.assoTypeLogement((TypeLogement) type.getSelectedItem());

                try {
                    logement.save();

                    Snackbar.make(v, "le logement a été correctement crée", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mAdapter.actualiser(Logement.findAll());
                } catch (Exception e) {
                    Snackbar.make(v, "echec", Snackbar.LENGTH_LONG)
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

                ref.setText("");
                desc.setText("");
                priwMax.setText("");
                prixMin.setText("");

                dialog.dismiss();

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void setupPagination() {
        // If RecyclerView was recently bound, unbind
        if (paginate != null) {
            paginate.unbind();
        }
        handler.removeCallbacks(fakeCallback);
        mAdapter = new LogementAdapter(this, (ArrayList<Logement>) Logement.findAll(), this);
        loading = false;
        page = 0;

        mAdapter = new LogementAdapter(this, Logement.getInitData(initItem), this);
        mRecyclerView.setAdapter(mAdapter);


        ((LogementAdapter) mAdapter).setMode(Attributes.Mode.Single);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.model, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onQueryTextChange(String query) {
        final List<Logement> filteredModelList = filter(Logement.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private List<Logement> filter(List<Logement> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<Logement> filteredModelList = new ArrayList<>();
        for (Logement model : models) {
            final String text = model.getReference().toLowerCase();
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
