package warofages.gamebackend;

import org.json.JSONArray;

/**
 * Created by mike on 6/5/2017.
 * Basically a callback to return the response to whoever needs it
 */

public interface DisplaysChanges {

        void displayPollResult(JSONArray result);

        void continueAfterTerrainLoaded();

}
