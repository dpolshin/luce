package foo.bar.luce.ui;

public class SearchResultModel {
    private String label;
    private String file;


    public SearchResultModel(String label, String file) {
        this.label = label;
        this.file = file;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
