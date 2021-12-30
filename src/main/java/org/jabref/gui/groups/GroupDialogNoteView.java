package org.jabref.gui.groups;

import com.airhacks.afterburner.views.ViewLoader;
import com.microsoft.applicationinsights.core.dependencies.google.api.Property;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.jabref.gui.DialogService;
import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.icon.JabrefIconProvider;
import org.jabref.gui.util.BaseDialog;
import org.jabref.gui.util.IconValidationDecorator;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.groups.AbstractGroup;
import org.jabref.model.groups.GroupHierarchyType;
import org.jabref.preferences.PreferencesService;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.ServiceLoader;

public class GroupDialogNoteView extends BaseDialog<AbstractGroup> {
    // Basic Settings
    @FXML
    private TextField groupNote;
    @FXML
    private Label addOrEditNote;
    private int edit;

    private final EnumMap<GroupHierarchyType, String> hierarchyText = new EnumMap<>(GroupHierarchyType.class);
    private final EnumMap<GroupHierarchyType, String> hierarchyToolTip = new EnumMap<>(GroupHierarchyType.class);

    private final ControlsFxVisualizer validationVisualizer = new ControlsFxVisualizer();
    private final GroupDialogViewModel viewModel;

    public GroupDialogNoteView(DialogService dialogService,
                               BibDatabaseContext currentDatabase,
                               PreferencesService preferencesService,
                               AbstractGroup editedGroup,
                               GroupDialogHeader groupDialogHeader) {
        viewModel = new GroupDialogViewModel(dialogService, currentDatabase, preferencesService, editedGroup, groupDialogHeader);
        this.edit = edit;
        ViewLoader.view(this)
                .load()
                .setAsDialogPane(this);

       /* if (editedGroup == null) {
            if (groupDialogHeader == GroupDialogHeader.GROUP) {
                this.setTitle(Localization.lang("Add group"));
            } else if (groupDialogHeader == GroupDialogHeader.SUBGROUP) {
                this.setTitle(Localization.lang("Add subgroup"));
            }
        } else {*/
        this.setTitle(Localization.lang("Group note"));
        //}

        setResultConverter(viewModel::resultConverter);
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        final Button confirmDialogButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        confirmDialogButton.disableProperty().bind(viewModel.validationStatus().validProperty().not());
        // handle validation before closing dialog and calling resultConverter
        confirmDialogButton.addEventFilter(ActionEvent.ACTION, viewModel::validationHandler);
    }

    @FXML
    public void initialize() {
        hierarchyText.put(GroupHierarchyType.INCLUDING, Localization.lang("Union"));
        hierarchyToolTip.put(GroupHierarchyType.INCLUDING, Localization.lang("Include subgroups: When selected, view entries contained in this group or its subgroups"));
        hierarchyText.put(GroupHierarchyType.REFINING, Localization.lang("Intersection"));
        hierarchyToolTip.put(GroupHierarchyType.REFINING, Localization.lang("Refine supergroup: When selected, view entries contained in both this group and its supergroup"));
        hierarchyText.put(GroupHierarchyType.INDEPENDENT, Localization.lang("Independent"));
        hierarchyToolTip.put(GroupHierarchyType.INDEPENDENT, Localization.lang("Independent group: When selected, view only this group's entries"));

        groupNote.textProperty().bindBidirectional(viewModel.noteProperty());
        System.out.println(viewModel.noteProperty().getValue().length());
        System.out.println(viewModel.noteProperty().getValue());
        String label = viewModel.noteProperty().getValue().length() == 0 ? "Add a note for this group: " : "Edit your current note for this group" ;
        addOrEditNote.setText(label);
        validationVisualizer.setDecoration(new IconValidationDecorator());
    }

    @FXML
    private void texGroupBrowse() {
        viewModel.texGroupBrowse();
    }

    @FXML
    private void openHelp() {
        viewModel.openHelpPage();
    }

    @FXML
    private void openIconPicker() {
        ObservableList<Ikon> ikonList = FXCollections.observableArrayList();
        FilteredList<Ikon> filteredList = new FilteredList<>(ikonList);

        for (IkonProvider provider : ServiceLoader.load(IkonProvider.class.getModule().getLayer(), IkonProvider.class)) {
            if (provider.getClass() != JabrefIconProvider.class) {
                ikonList.addAll(EnumSet.allOf(provider.getIkon()));
            }
        }

        CustomTextField searchBox = new CustomTextField();
        searchBox.setPromptText(Localization.lang("Search") + "...");
        searchBox.setLeft(IconTheme.JabRefIcons.SEARCH.getGraphicNode());
        searchBox.textProperty().addListener((obs, oldValue, newValue) ->
                filteredList.setPredicate(ikon -> newValue.isEmpty() || ikon.getDescription().toLowerCase()
                        .contains(newValue.toLowerCase())));

        GridView<Ikon> ikonGridView = new GridView<>(FXCollections.observableArrayList());
        ikonGridView.setCellFactory(gridView -> new IkonliCell());
        ikonGridView.setPrefWidth(520);
        ikonGridView.setPrefHeight(400);
        ikonGridView.setHorizontalCellSpacing(4);
        ikonGridView.setVerticalCellSpacing(4);
        ikonGridView.setItems(filteredList);

        VBox vBox = new VBox(10, searchBox, ikonGridView);
        vBox.setPadding(new Insets(10));

        PopOver popOver = new PopOver(vBox);
        popOver.setDetachable(false);
        popOver.setArrowSize(0);
        popOver.setCornerRadius(0);
        popOver.setTitle("Icon picker");
    }

    public class IkonliCell extends GridCell<Ikon> {
        @Override
        protected void updateItem(Ikon ikon, boolean empty) {
            super.updateItem(ikon, empty);
            if (empty || ikon == null) {
                setText(null);
                setGraphic(null);
            } else {
                FontIcon fontIcon = FontIcon.of(ikon);
                fontIcon.getStyleClass().setAll("font-icon");
                fontIcon.setIconSize(22);
                setGraphic(fontIcon);
                setAlignment(Pos.BASELINE_CENTER);
                setPadding(new Insets(1));
                setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));

                setOnMouseClicked(event -> {
                    PopOver stage = (PopOver) this.getGridView().getParent().getScene().getWindow();
                    stage.hide();
                });
            }
        }
    }
}
