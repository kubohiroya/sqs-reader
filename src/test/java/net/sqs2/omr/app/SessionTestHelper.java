package net.sqs2.omr.app;

import java.io.IOException;

import net.sqs2.omr.model.SessionSource;
import net.sqs2.omr.model.SessionSourceManager;
import net.sqs2.omr.session.init.SessionSourceInitServiceTest;

/**
 * This class is used to close the session, all tests should use it so that is
 * possible to change behavior remove / do not remove.
 * 
 * 
 */
public class SessionTestHelper
{
    static final boolean REMOVE = false;

    private SessionTestHelper()
    {
        //
    }

    /**
     * All tests must call this method to close the {@link SessionSourceManager},
     * instead of using directly {@link SessionSourceManager#closeAll(boolean)}.
     * 
     * @throws IOException
     */
    public static void closeSessionSources() throws IOException
    {
        if (REMOVE)
        {
            // Original Code:
            SessionSourceManager.closeAll(true);
        }
        else
        {
            SessionSourceManager.closeAll(false);
        }

    }

    /**
     * Only used by {@link SessionSourceInitServiceTest}
     * 
     * @param sessionSource
     * @throws IOException
     */
    public static void closeSession(SessionSource sessionSource) throws IOException
    {
        if (REMOVE) {
            // Original Code:
            sessionSource.close();
            sessionSource.removeResultDirectories();
        } else {
            sessionSource.close();
        }
    }

}
