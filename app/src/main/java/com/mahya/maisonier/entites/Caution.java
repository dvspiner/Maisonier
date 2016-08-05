/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mahya.maisonier.entites;


import android.support.annotation.Size;

import com.mahya.maisonier.dataBase.Maisonier;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.Date;

@ModelContainer
@Table(database = Maisonier.class, useBooleanGetterSetters = true)
public class Caution extends BaseModel {


    @PrimaryKey(autoincrement = true)
    @Column(name = "id")
    Integer id;

    @NotNull
    @Column(name = "date_depot")

    Date dateDepot;

    @NotNull
    @Column(name = "montant")
    double montant;

    @NotNull
    @Column(name = "montant_rembourse")
    double montantRembourse;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "statut", length = 255)
    String statut;


    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "occupation_id",
                    columnType = Integer.class,
                    foreignKeyColumnName = "id")},
            saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE)
    ForeignKeyContainer<Occupation> occupation;

    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "typecaution_id",
                    columnType = Integer.class,
                    foreignKeyColumnName = "id")},
            saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE)
    ForeignKeyContainer<TypeCaution> typeCaution;

    public Caution() {
    }

    public Caution(Integer id) {
        this.id = id;
    }

    public Caution(Integer id, Date dateDepot, double montant, double montantRembourse, String statut) {
        this.id = id;
        this.dateDepot = dateDepot;
        this.montant = montant;
        this.montantRembourse = montantRembourse;
        this.statut = statut;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(Date dateDepot) {
        this.dateDepot = dateDepot;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getMontantRembourse() {
        return montantRembourse;
    }

    public void setMontantRembourse(double montantRembourse) {
        this.montantRembourse = montantRembourse;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void assoOccupation(Occupation occupation1) {
        occupation = new ForeignKeyContainer<>(Occupation.class);
        occupation.setModel(occupation1);
        occupation.put(Occupation_Table.id, occupation1.id);

    }

    public void assoTypecaution(TypeCaution typeCaution1) {
        typeCaution = new ForeignKeyContainer<>(TypeCaution.class);
        typeCaution.setModel(typeCaution1);
        typeCaution.put(Occupation_Table.id, typeCaution1.id);

    }

}
