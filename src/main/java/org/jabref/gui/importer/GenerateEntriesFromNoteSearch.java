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
import org.jabref.model.groups.AbstractGroup;
import org.jabref.model.groups.GroupTreeNode;
import org.jabref.preferences.PreferencesService;

import java.util.List;
import java.util.Optional;

public class GenerateEntriesFromNoteSearch {
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

    private PopOver noteFromPopOver;

    public GenerateEntriesFromNoteSearch(LibraryTab libraryTab, DialogService dialogService, PreferencesService preferencesService, TaskExecutor taskExecutor, StateManager stateManager) {
        ViewLoader.view(this).load();
        this.preferencesService = preferencesService;
        this.dialogService = dialogService;
        this.libraryTab = libraryTab;
        this.taskExecutor = taskExecutor;
        this.stateManager = stateManager;
        this.generateButton.setGraphic(IconTheme.JabRefIcons.SEARCH.getGraphicNode());
        this.generateButton.setDefaultButton(true);
    }

    public void setListView()
    {
        String note = idTextField.getText();
        if(note.isEmpty()){
            dialogService.notify(Localization.lang("Write a note"));
            return;
        }

        idTextField.requestFocus();
        BibDatabaseContext database = stateManager.getActiveDatabase().orElseThrow(NullPointerException::new);
        groupTreeNode = stateManager.getSelectedGroup(database).get(0);
        List<BibEntry> allEntries = database.getEntries();

        while(!groupTreeNode.isRoot()) {
            Optional<GroupTreeNode> newNode = groupTreeNode.getParent();
            groupTreeNode = newNode.get();
        }
        entriesName.clear();
        ObservableList<GroupTreeNode> groups = groupTreeNode.getChildren();
        cycleForAllGroups(groups, note, allEntries);

        listOfEntries.setItems(entriesName);

        //System.out.println(stateManager.getSelectedGroup(database).get(0));
    }

    public void setNoteFromPopOver(PopOver noteFromPopOver){
        this.noteFromPopOver = noteFromPopOver;
    }

    public DialogPane getDialogPane() {
        return dialogPane;
    }

    private void putEntriesInObservableList(List<BibEntry> entriesOfGroup) {
        for(int i = 0; i < entriesOfGroup.size();i++){
            if(entriesOfGroup.get(i).getTitle().isEmpty())
                entriesName.add("Untitled " + entriesOfGroup.get(i).getType().getName());
            else {
                String entryTitle = entriesOfGroup.get(i).getTitle().get();
                entriesName.add(entryTitle);
            }

        }
    }

    private void cycleForAllGroups(ObservableList<GroupTreeNode> groups, String note, List<BibEntry> allEntries) {
        for(int i = 0; i < groups.size(); i++){
            GroupTreeNode g =groups.get(i);
            AbstractGroup ag = g.getGroup();
            String groupNote = ag.getNote().get();
            if(!groupNote.isEmpty() && groupNote.contains(note)){
                List<BibEntry> entriesOfGroup = g.getEntriesInGroup(allEntries);
                putEntriesInObservableList(entriesOfGroup);
            }
            if(g.getChildren().size() > 0) cycleForAllGroups(g.getChildren(), note, allEntries);
        }
    }

}
