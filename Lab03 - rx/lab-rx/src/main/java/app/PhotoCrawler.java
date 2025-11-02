package app;

import io.reactivex.rxjava3.core.Observable;
import model.Photo;
import util.PhotoDownloader;
import util.PhotoProcessor;
import util.PhotoSerializer;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhotoCrawler {

    private static final Logger log = Logger.getLogger(PhotoCrawler.class.getName());

    private final PhotoDownloader photoDownloader;

    private final PhotoSerializer photoSerializer;

    private final PhotoProcessor photoProcessor;

    public PhotoCrawler() throws IOException {
        this.photoDownloader = new PhotoDownloader();
        this.photoSerializer = new PhotoSerializer("./photos");
        this.photoProcessor = new PhotoProcessor();
    }

    public void resetLibrary() throws IOException {
        photoSerializer.deleteLibraryContents();
    }

    public void downloadPhotoExamples() {
        photoDownloader.getPhotoExamples()
                .subscribe(
                        photo -> {
                            photoSerializer.savePhoto(photo);
                            log.info("Saved photo: " + photo);
                        },
                        error -> {
                            log.log(Level.SEVERE, "Error downloading photo examples", error);
                        },
                        () -> {
                            log.info("All example photos downloaded successfully.");
                        }
                );
    }

    public void downloadPhotosForQuery(String query) {
        photoDownloader.searchForPhotos(query)
                .subscribe(
                        photo -> {
                            photoSerializer.savePhoto(photo);
                            log.info("Saved photo for query '" + query + "'");
                        },
                        error -> {
                            log.log(Level.SEVERE, "Error while searching photos for query: " + query, error);
                        },
                        () -> {
                            log.info("All photos for query '" + query + "' downloaded successfully.");
                        }
                );
    }

    public void downloadPhotosForMultipleQueries(List<String> queries) {
        Observable.fromIterable(queries)
                .flatMap(query -> {
                    log.info("Starting download for query: " + query);
                    return photoDownloader.searchForPhotos(query);
                })
                .subscribe(
                        photo -> {
                            photoSerializer.savePhoto(photo);
                            log.info("Saved photo from multi-query download");
                        },
                        error -> {
                            log.log(Level.SEVERE, "Error while downloading photos for multiple queries", error);
                        },
                        () -> {
                            log.info("All photos for all queries downloaded successfully.");
                        }
                );
    }
}
