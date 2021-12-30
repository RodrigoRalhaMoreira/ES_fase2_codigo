package org.jabref.gui.importer;

import com.airhacks.afterburner.views.ViewLoader;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.controlsfx.control.PopOver;
import org.jabref.gui.DialogService;
import org.jabref.gui.LibraryTab;
import org.jabref.gui.StateManager;
import org.jabref.gui.groups.GroupNodeViewModel;
import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.groups.GroupTreeNode;
import org.jabref.preferences.PreferencesService;

import java.util.List;

public class GenerateAuthorsFromCountry {
    @FXML
    DialogPane dialogPane;
    @FXML
    TextField idTextField;
    @FXML
    Button generateButton;
    @FXML
    private ListView<String> listOfEntries = new ListView<>();

    ObservableList<String> entriesName = FXCollections.observableArrayList();

    private final PreferencesService preferencesService;
    private final DialogService dialogService;
    private final LibraryTab libraryTab;
    private final TaskExecutor taskExecutor;
    private final StateManager stateManager;
    private GroupTreeNode groupTreeNode;
    ObjectProperty<GroupNodeViewModel> rootGroup;

    private PopOver authorPopOver;

    public GenerateAuthorsFromCountry(LibraryTab libraryTab, DialogService dialogService, PreferencesService preferencesService, TaskExecutor taskExecutor, StateManager stateManager) {
        ViewLoader.view(this).load();
        this.preferencesService = preferencesService;
        this.dialogService = dialogService;
        this.libraryTab = libraryTab;
        this.taskExecutor = taskExecutor;
        this.stateManager = stateManager;
        this.generateButton.setGraphic(IconTheme.JabRefIcons.PRIORITY.getGraphicNode());
        this.generateButton.setDefaultButton(true);
    }

    public void setListView()
    {
        String country = idTextField.getText();
        if(country.isEmpty()){
            dialogService.notify(Localization.lang("Write a country"));
            return;
        }

        idTextField.requestFocus();
        BibDatabaseContext database = stateManager.getActiveDatabase().orElseThrow(NullPointerException::new);

        List<BibEntry> allEntries = database.getEntries();

        putEntriesInObservableList(allEntries, country);

        listOfEntries.setItems(entriesName);
    }

    public void setNoteFromPopOver(PopOver noteFromPopOver){
        this.authorPopOver = noteFromPopOver;
    }

    public DialogPane getDialogPane() {
        return dialogPane;
    }


    private void putEntriesInObservableList(List<BibEntry> entries, String country) {
        entriesName.remove(0,entriesName.size());
        for(int i = 0; i < entries.size();i++){
            BibEntry entry = entries.get(i);
            if(entry.getField(StandardField.COUNTRY).isEmpty())
                continue;
            String countryOfEntry = entry.getField(StandardField.COUNTRY).get();
            if(country.equalsIgnoreCase(countryOfEntry)) {
                if(entry.getField(StandardField.AUTHOR).isEmpty())
                    entriesName.add("Unnamed Author");
                else{
                    String author = entry.getField(StandardField.AUTHOR).get();
                    entriesName.add(author);
                }
            }
        }
    }

}
