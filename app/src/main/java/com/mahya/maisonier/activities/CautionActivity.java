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
import android.transition.ChangeTransform;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.CautionAdapter;
import com.mahya.maisonier.entites.Caution;
import com.mahya.maisonier.entites.Caution_Table;
import com.mahya.maisonier.entites.Habitant;
import com.mahya.maisonier.entites.Occupation;
import com.mahya.maisonier.entites.TypeCaution;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.mahya.maisonier.utils.MyRecyclerScroll;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import me.srodrigo.androidhintspinner.HintAdapter;
import me.srodrigo.androidhintspinner.HintSpinner;

import static com.mahya.maisonier.utils.Utils.currentDate;

public class CautionActivity extends BaseActivity implements CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {


    private static final String TAG = CautionActivity.class.getSimpleName();
    protected RecyclerView mRecyclerView;
    CautionAdapter mAdapter;
    FrameLayout fab;
    FloatingActionButton myfab_main_btn;
    Animation animation;
    DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = this;
    private TextView tvEmptyView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        super.setContentView(R.layout.activity_model1);
        Caution.cautions.clear();
        Caution.cautions = Caution.findAll();
        setTitle(context.getString(R.string.Caution));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        fab.startAnimation(animation);
        mAdapter = new CautionAdapter(this, (ArrayList<Caution>) Caution.findAll(), this);
        myfab_main_btn.hide(false);
        mRecyclerView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myfab_main_btn.show(true);
                myfab_main_btn.setShowAnimation(AnimationUtils.loadAnimation(context, R.anim.show_from_bottom));
                myfab_main_btn.setHideAnimation(AnimationUtils.loadAnimation(context, R.anim.hide_to_bottom));
            }
        }, 300);
        mRecyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                myfab_main_btn.show(true);
            }

            @Override
            public void hide() {
                myfab_main_btn.hide(true);
            }
        });

    }

    private void initView() {

        fab = (FrameLayout) findViewById(R.id.myfab_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_item);
        tvEmptyView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView.setFilterTouchesWhenObscured(true);
        myfab_main_btn = (FloatingActionButton) findViewById(R.id.myfab_main_btn);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        if (Caution.findAll().isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }


    }

    public void action(final View view) {
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
        dialog.setContentView(R.layout.add_cautions);
        // Initialisation du formulaire

        final TextView mOperation = (TextView) dialog.findViewById(R.id.operation);
        final Spinner mHabitant = (Spinner) dialog.findViewById(R.id.Habitant);
        final Spinner mLogement = (Spinner) dialog.findViewById(R.id.Logement);
        final Spinner mTypeDeCaution = (Spinner) dialog.findViewById(R.id.TypeDeCaution);
        final EditText mMontantPaye = (EditText) dialog.findViewById(R.id.MontantPaye);
        final EditText mDateDeDepot = (EditText) dialog.findViewById(R.id.DateDeDepot);
        final Button mDateSelect = (Button) dialog.findViewById(R.id.dateSelect);
        final MaterialBetterSpinner mStatut = (MaterialBetterSpinner) dialog.findViewById(R.id.Statut);
        List<String> statuts = new ArrayList<>();
        statuts.add("Enregistrée");
        statuts.add("Remboursée");
        statuts.add("Retenue");
        mStatut.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, statuts));

        mDateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                final DatePicker datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                Button changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                mDateDeDepot.setText(currentDate(datePicker));
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDateDeDepot.setText(currentDate(datePicker));
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDateDeDepot.setText(currentDate(datePicker));
                        dialog1.dismiss();
                    }
                });
            }
        });

        final HintSpinner habitantHint = new HintSpinner<>(
                mHabitant,
                new HintAdapter<Habitant>(context, "Habitant ", Habitant.findAll()),
                new HintSpinner.Callback<Habitant>() {


                    @Override
                    public void onItemSelected(int position, Habitant itemAtPosition) {


                        final HintSpinner typeHint = new HintSpinner<>(
                                mLogement,
                                new HintAdapter<Occupation>(context, "Logement ", itemAtPosition.getOccupationList()),
                                new HintSpinner.Callback<Occupation>() {


                                    @Override
                                    public void onItemSelected(int position, Occupation itemAtPosition) {


                                    }
                                });
                        typeHint.init();

                    }
                });
        habitantHint.init();


        final HintSpinner cautionHint = new HintSpinner<>(
                mTypeDeCaution,
                new HintAdapter<TypeCaution>(this, "Type de caution ", TypeCaution.findAll()),
                new HintSpinner.Callback<TypeCaution>() {


                    @Override
                    public void onItemSelected(int position, TypeCaution itemAtPosition) {


                    }
                });
        cautionHint.init();


        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (mHabitant.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un habitant", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mLogement.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un logement", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mTypeDeCaution.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un type de caution", Toast.LENGTH_SHORT).show();
                    return;

                }


                Caution caution = new Caution();
                try {
                    caution.setDateCaution(sdf.parse(mDateDeDepot.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                caution.assoOccupation((Occupation) mLogement.getSelectedItem());
                caution.setStatut(mStatut.getText().toString());
                caution.setMontant(Double.parseDouble(mMontantPaye.getText().toString().trim()));
                caution.assoTypecaution(((TypeCaution) mTypeDeCaution.getSelectedItem()));

                try {
                    caution.save();
                    if (Caution.findAll().isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        tvEmptyView.setVisibility(View.VISIBLE);

                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        tvEmptyView.setVisibility(View.GONE);
                    }

                    Snackbar.make(view, "la Caution a été correctement crée", Snackbar.LENGTH_LONG)

                            .setAction("Action", null).show();
                    mAdapter.addItem(0, caution);
                } catch (Exception e) {
                    e.printStackTrace();
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

                mMontantPaye.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        //FlowManager.destroy();
        // Delete.tables(Caution.class);
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

                            Caution Caution = new Caution();
                            Caution.setId(id);
                            Caution.delete();

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


    }

    @Override
    public void modifier(final int id) {

        final Caution caution = SQLite.select().from(Caution.class).where(Caution_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_cautions);
        // Initialisation du formulaire

        final TextView mOperation = (TextView) dialog.findViewById(R.id.operation);
        final Spinner mHabitant = (Spinner) dialog.findViewById(R.id.Habitant);
        final Spinner mLogement = (Spinner) dialog.findViewById(R.id.Logement);
        final Spinner mTypeDeCaution = (Spinner) dialog.findViewById(R.id.TypeDeCaution);
        final EditText mMontantPaye = (EditText) dialog.findViewById(R.id.MontantPaye);
        final EditText mDateDeDepot = (EditText) dialog.findViewById(R.id.DateDeDepot);
        final Button mDateSelect = (Button) dialog.findViewById(R.id.dateSelect);
        final MaterialBetterSpinner mStatut = (MaterialBetterSpinner) dialog.findViewById(R.id.Statut);
        mOperation.setText("Modifier une caution");
        mDateSelect.setText(sdf.format(caution.getDateCaution()));
        mMontantPaye.setText(String.valueOf(caution.getMontant()));
        List<String> statuts = new ArrayList<>();
        statuts.add("Enregistrée");
        statuts.add("Remboursée");
        statuts.add("Retenue");
        mStatut.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, statuts));

        final HintSpinner habitantHint = new HintSpinner<>(
                mHabitant,
                new HintAdapter<Habitant>(context, "Habitant ", Habitant.findAll()),
                new HintSpinner.Callback<Habitant>() {


                    @Override
                    public void onItemSelected(int position, Habitant itemAtPosition) {


                        final HintSpinner typeHint = new HintSpinner<>(
                                mLogement,
                                new HintAdapter<Occupation>(context, "Logement ", itemAtPosition.getOccupationList()),
                                new HintSpinner.Callback<Occupation>() {


                                    @Override
                                    public void onItemSelected(int position, Occupation itemAtPosition) {


                                    }
                                });
                        typeHint.init();

                    }
                });
        habitantHint.init();


        final HintSpinner cautionHint = new HintSpinner<>(
                mTypeDeCaution,
                new HintAdapter<TypeCaution>(this, "Type de caution ", TypeCaution.findAll()),
                new HintSpinner.Callback<TypeCaution>() {


                    @Override
                    public void onItemSelected(int position, TypeCaution itemAtPosition) {


                    }
                });
        cautionHint.init();


        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (mHabitant.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un habitant", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mLogement.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un logement", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mTypeDeCaution.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Veillez selectionner un type de caution", Toast.LENGTH_SHORT).show();
                    return;

                }


                Caution caution = new Caution();
                try {
                    caution.setDateCaution(sdf.parse(mDateDeDepot.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                caution.assoOccupation((Occupation) mLogement.getSelectedItem());
                caution.setId(id);
                caution.setMontant(Double.parseDouble(mMontantPaye.getText().toString().trim()));
                caution.assoTypecaution(((TypeCaution) mTypeDeCaution.getSelectedItem()));

                try {
                    caution.save();
                    if (Caution.findAll().isEmpty()) {
                        mRecyclerView.setVisibility(View.GONE);
                        tvEmptyView.setVisibility(View.VISIBLE);

                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        tvEmptyView.setVisibility(View.GONE);
                    }

                    Snackbar.make(v, "la Caution a été correctement modifié", Snackbar.LENGTH_LONG)

                            .setAction("Action", null).show();
                    mAdapter.addItem(0, caution);
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

                mMontantPaye.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
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
        final List<Caution> filteredModelList = filter(Caution.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private List<Caution> filter(List<Caution> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<Caution> filteredModelList = new ArrayList<>();
        for (Caution model : models) {
            final String text = model.getOccupation().load().getHabitant().load().getNom().toLowerCase();
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
