package org.jabref.gui.linkedfile;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import org.jabref.gui.AbstractViewModel;
import org.jabref.gui.DialogService;
import org.jabref.gui.externalfiletype.ExternalFileType;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
import org.jabref.gui.externalfiletype.UnknownExternalFileType;
import org.jabref.gui.frame.ExternalApplicationsPreferences;
import org.jabref.gui.util.FileDialogConfiguration;
import org.jabref.logic.FilePreferences;
import org.jabref.logic.util.io.FileUtil;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.LinkedFile;

import com.tobiasdiez.easybind.EasyBind;
import com.tobiasdiez.easybind.optional.ObservableOptionalValue;

public class LinkedFileEditDialogViewModel extends AbstractViewModel {

    private static final Pattern REMOTE_LINK_PATTERN = Pattern.compile("[a-z]+://.*");
    private final StringProperty link = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty sourceUrl = new SimpleStringProperty("");
    private final ListProperty<ExternalFileType> allExternalFileTypes = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private final ObjectProperty<ExternalFileType> selectedExternalFileType = new SimpleObjectProperty<>();
    private final ObservableOptionalValue<ExternalFileType> monadicSelectedExternalFileType;
    private final BibDatabaseContext database;
    private final DialogService dialogService;
    private final ExternalApplicationsPreferences externalApplicationsPreferences;
    private final FilePreferences filePreferences;

    public LinkedFileEditDialogViewModel(LinkedFile linkedFile,
                                         BibDatabaseContext database,
                                         DialogService dialogService,
                                         ExternalApplicationsPreferences externalApplicationsPreferences,
                                         FilePreferences filePreferences) {
        this.database = database;
        this.dialogService = dialogService;
        this.filePreferences = filePreferences;
        this.externalApplicationsPreferences = externalApplicationsPreferences;
        allExternalFileTypes.set(FXCollections.observableArrayList(externalApplicationsPreferences.getExternalFileTypes()));

        monadicSelectedExternalFileType = EasyBind.wrapNullable(selectedExternalFileType);
        setValues(linkedFile);
    }

    private void setExternalFileTypeByExtension(String link) {
        if (!link.isEmpty()) {
            // Check if this looks like a remote link:
            if (REMOTE_LINK_PATTERN.matcher(link).matches()) {
                ExternalFileTypes.getExternalFileTypeByExt("html", externalApplicationsPreferences)
                                 .ifPresent(selectedExternalFileType::setValue);
            }

            // Try to guess the file type:
            String theLink = link.trim();
            ExternalFileTypes.getExternalFileTypeForName(theLink, externalApplicationsPreferences)
                             .ifPresent(selectedExternalFileType::setValue);
        }
    }

    public void openBrowseDialog() {
        String fileText = link.get();

        Optional<Path> file = FileUtil.find(database, fileText, filePreferences);

        Path workingDir = file.orElse(filePreferences.getWorkingDirectory());
        String fileName = Path.of(fileText).getFileName().toString();

        FileDialogConfiguration fileDialogConfiguration = new FileDialogConfiguration.Builder()
                .withInitialDirectory(workingDir)
                .withInitialFileName(fileName)
                .build();

        dialogService.showFileOpenDialog(fileDialogConfiguration).ifPresent(path -> {
            // Store the directory for next time:
            filePreferences.setWorkingDirectory(path);
            link.set(relativize(path));

            setExternalFileTypeByExtension(link.getValueSafe());
        });
    }

    public void setValues(LinkedFile linkedFile) {
        description.set(linkedFile.getDescription());
        sourceUrl.set(linkedFile.getSourceUrl());

        if (linkedFile.isOnlineLink()) {
            link.setValue(linkedFile.getLink()); // Might be an URL
        } else {
            link.setValue(relativize(Path.of(linkedFile.getLink())));
        }

        // See what is a reasonable selection for the type combobox:
        Optional<ExternalFileType> fileType = ExternalFileTypes.getExternalFileTypeByLinkedFile(linkedFile, false, externalApplicationsPreferences);
        if (fileType.isPresent() && !(fileType.get() instanceof UnknownExternalFileType)) {
            selectedExternalFileType.setValue(fileType.get());
        } else if ((linkedFile.getLink() != null) && (!linkedFile.getLink().isEmpty())) {
            setExternalFileTypeByExtension(linkedFile.getLink());
        }
    }

    public StringProperty linkProperty() {
        return link;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty sourceUrlProperty() {
        return sourceUrl;
    }

    public ListProperty<ExternalFileType> externalFileTypeProperty() {
        return allExternalFileTypes;
    }

    public ObjectProperty<ExternalFileType> selectedExternalFileTypeProperty() {
        return selectedExternalFileType;
    }

    public LinkedFile getNewLinkedFile() {
        String fileType = monadicSelectedExternalFileType.getValue().map(ExternalFileType::toString).orElse("");

        if (LinkedFile.isOnlineLink(link.getValue())) {
            try {
                return new LinkedFile(description.getValue(), URI.create(link.getValue()).toURL(), fileType, sourceUrl.getValue());
            } catch (MalformedURLException e) {
                return new LinkedFile(description.getValue(), link.getValue(), fileType, sourceUrl.getValue());
            }
        }
        return new LinkedFile(description.getValue(), Path.of(link.getValue()), fileType, sourceUrl.getValue());
    }

    private String relativize(Path filePath) {
        return FileUtil.relativize(filePath, database, filePreferences).toString();
    }
}