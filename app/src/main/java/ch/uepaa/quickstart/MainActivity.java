package ch.uepaa.quickstart;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ch.uepaa.p2pkit.AlreadyEnabledException;
import ch.uepaa.p2pkit.P2PKit;
import ch.uepaa.p2pkit.P2PKitStatusListener;
import ch.uepaa.p2pkit.StatusResult;
import ch.uepaa.p2pkit.discovery.DiscoveryInfoTooLongException;
import ch.uepaa.p2pkit.discovery.DiscoveryInfoUpdatedTooOftenException;
import ch.uepaa.p2pkit.discovery.DiscoveryListener;
import ch.uepaa.p2pkit.discovery.DiscoveryPowerMode;
import ch.uepaa.p2pkit.discovery.Peer;
import ch.uepaa.p2pkit.discovery.ProximityStrength;
import ch.uepaa.quickstart.fragments.ColorPickerFragment;
import ch.uepaa.quickstart.fragments.ConsoleFragment;
import ch.uepaa.quickstart.graph.Graph;
import ch.uepaa.quickstart.graph.GraphView;
import ch.uepaa.quickstart.utils.ColorStorage;
import ch.uepaa.quickstart.utils.Logger;

public class MainActivity extends AppCompatActivity implements ConsoleFragment.ConsoleListener, ColorPickerFragment.ColorPickerListener {

    private static final String APP_KEY = "<CHANGE ME>";

    public void enableP2PKit() {
        try {
            Logger.i("P2PKit", "Enabling p2pkit");
            P2PKit.enable(this, APP_KEY, p2pKitStatusListener);
        } catch (AlreadyEnabledException e) {
            Logger.w("P2PKit", "p2pkit is already enabled " + e.getLocalizedMessage());
        }
    }

    private final P2PKitStatusListener p2pKitStatusListener = new P2PKitStatusListener() {

        @Override
        public void onEnabled() {
            Logger.i("P2PKitStatusListener", "Successfully enabled p2pkit");

            UUID ownNodeId = P2PKit.getMyPeerId();
            setupPeers(ownNodeId);
            startDiscovery();
        }

        @Override
        public void onDisabled() {
            Logger.i("P2PKitStatusListener", "p2pkit disabled");
        }

        @Override
        public void onError(StatusResult statusResult) {
            handleStatusResult(statusResult);
            Logger.e("P2PKitStatusListener", "p2pkit lifecycle error with code: " + statusResult.getStatusCode());
        }

        @Override
        public void onException(Throwable throwable) {
            String errorMessage = "An error occurred and p2pkit stopped, please try again.";
            showError(errorMessage, true);
            Logger.e("P2PKitStatusListener", "p2pkit threw an exception: " + Log.getStackTraceString(throwable));
            teardownPeers();
        }
    };

    public void disableP2PKit() {
        Logger.i("P2PKit", "Disable p2pkit");

        if (P2PKit.isEnabled()) {
            P2PKit.disable();
            teardownPeers();
        }
    }

    @Override
    public void startDiscovery() {
        Logger.i("P2PKit", "Start discovery");

        byte[] ownDiscoveryData = loadOwnDiscoveryData();

        try {
            P2PKit.enableProximityRanging();
            P2PKit.startDiscovery(ownDiscoveryData, DiscoveryPowerMode.HIGH_PERFORMANCE, mDiscoveryListener);
        } catch (DiscoveryInfoTooLongException e) {
            Logger.w("P2PKit", "Can not start discovery, discovery info is to long " + e.getLocalizedMessage());
        }
    }

    @Override
    public void stopDiscovery() {
        Logger.i("P2PKit", "Stop discovery");
        P2PKit.stopDiscovery();

        for (Peer peer : nearbyPeers) {
            handlePeerLost(peer);
        }

        nearbyPeers.clear();
    }

    private boolean pushNewDiscoveryInfo(byte[] data) {
        Logger.i("P2PKit", "Push new discovery info");
        boolean success = false;

        try {
            P2PKit.pushDiscoveryInfo(data);
            success = true;
        } catch (DiscoveryInfoTooLongException e) {
            Logger.e("P2PKit", "Failed to push new discovery info, info too long: " + e.getLocalizedMessage());
        } catch (DiscoveryInfoUpdatedTooOftenException e) {
            Logger.e("P2PKit", "Failed to push new discovery info due to throttling: " + e.getLocalizedMessage());
        }

        return success;
    }

    // Discovery events listener
    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onStateChanged(final int state) {
            handleDiscoveryStateChange(state);
            Logger.i("DiscoveryListener", "Discovery state changed: " + state);
        }

        @Override
        public void onPeerDiscovered(final Peer peer) {
            Logger.i("DiscoveryListener", "Peer discovered: " + peer.getPeerId() + ". Proximity strength: " + peer.getProximityStrength());
            nearbyPeers.add(peer);
            handlePeerDiscovered(peer);
        }

        @Override
        public void onPeerLost(final Peer peer) {
            Logger.i("DiscoveryListener", "Peer lost: " + peer.getPeerId());
            nearbyPeers.remove(peer);
            handlePeerLost(peer);
        }

        @Override
        public void onPeerUpdatedDiscoveryInfo(Peer peer) {
            Logger.i("DiscoveryListener", "Peer updated discovery info: " + peer.getPeerId());
            handlePeerUpdatedDiscoveryInfo(peer);
        }

        @Override
        public void onProximityStrengthChanged(Peer peer) {
            Logger.i("DiscoveryListener", "Peer changed proximity strength: " + peer.getPeerId() + ". Proximity strength: " + peer.getProximityStrength());
            handlePeerChangedProximityStrength(peer);
        }
    };

    private ColorStorage storage;
    private int defaultColor;
    private GraphView graphView;
    private final Set<Peer> nearbyPeers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        defaultColor = getResources().getColor(R.color.graph_node);
        storage = new ColorStorage(this);

        graphView = (GraphView) findViewById(R.id.graph);

        FloatingActionButton colorActionButton = (FloatingActionButton) findViewById(R.id.color_action);
        colorActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        enableP2PKit();
    }

    @Override
    public void onDestroy() {

        disableP2PKit();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (P2PKit.isEnabled()) {
            P2PKit.setDiscoveryPowerMode(DiscoveryPowerMode.HIGH_PERFORMANCE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (P2PKit.isEnabled()) {
            P2PKit.setDiscoveryPowerMode(DiscoveryPowerMode.LOW_POWER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (BuildConfig.BUILD_CONFIGURATION.equals("prod")) {
            hideSystemBars();
        }

        if (P2PKit.isEnabled()) {
            handleDiscoveryStateChange(P2PKit.getDiscoveryState());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_console:
                showConsole();
                return true;

            case R.id.action_enablekit:
                if (P2PKit.isEnabled()) {
                    disableP2PKit();
                } else {
                    enableP2PKit();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupPeers(final UUID ownNodeId) {
        byte[] ownDiscoveryData = loadOwnDiscoveryData();
        int ownColorCode = ColorStorage.getOrCreateColorCode(ownDiscoveryData, ColorStorage.createRandomColor());

        if (ownDiscoveryData == null) {
            storage.saveColor(ownColorCode);
        }

        Graph graph = graphView.getGraph();
        graph.setup(ownNodeId);
        graph.addNode(ownNodeId);
        graph.setNodeColor(ownNodeId, ownColorCode);
    }

    private void handlePeerDiscovered(final Peer peer) {
        UUID peerId = peer.getPeerId();
        byte[] peerDiscoveryInfo = peer.getDiscoveryInfo();
        int peerColor = ColorStorage.getOrCreateColorCode(peerDiscoveryInfo, defaultColor);
        float proximityStrength = (peer.getProximityStrength() - 1f) / 4;
        boolean proximityStrengthImmediate = peer.getProximityStrength() == ProximityStrength.IMMEDIATE;

        Graph graph = graphView.getGraph();
        graph.addNode(peerId);
        graph.setNodeColor(peerId, peerColor);
        graph.setEdgeStrength(peerId, proximityStrength);
        graph.setHighlighted(peerId, proximityStrengthImmediate);
    }

    private void handlePeerLost(final Peer peer) {
        UUID peerId = peer.getPeerId();
        Graph graph = graphView.getGraph();
        graph.removeNode(peerId);
        graph.updateOwnNode();
    }

    private void handlePeerUpdatedDiscoveryInfo(final Peer peer) {
        UUID peerId = peer.getPeerId();
        byte[] peerDiscoveryInfo = peer.getDiscoveryInfo();

        int peerColor = ColorStorage.getOrCreateColorCode(peerDiscoveryInfo, defaultColor);

        Graph graph = graphView.getGraph();
        graph.setNodeColor(peerId, peerColor);
    }

    private void handlePeerChangedProximityStrength(final Peer peer) {
        UUID peerId = peer.getPeerId();
        float proximityStrength = (peer.getProximityStrength() - 1f) / 4;
        boolean proximityStrengthImmediate = peer.getProximityStrength() == ProximityStrength.IMMEDIATE;

        Graph graph = graphView.getGraph();
        graph.setEdgeStrength(peerId, proximityStrength);
        graph.setHighlighted(peerId, proximityStrengthImmediate);
    }

    private void updateOwnDiscoveryInfo(int colorCode) {
        if (!P2PKit.isEnabled()) {
            Toast.makeText(this, R.string.p2pkit_not_enabled, Toast.LENGTH_LONG).show();
            return;
        }

        byte[] newColorBytes = ColorStorage.getColorBytes(colorCode);

        if (!pushNewDiscoveryInfo(newColorBytes)) {
            Toast.makeText(this, R.string.p2pkit_discovery_info_update_failed, Toast.LENGTH_LONG).show();
            return;
        }

        storage.saveColor(colorCode);
        Graph graph = graphView.getGraph();
        UUID ownNodeId = P2PKit.getMyPeerId();
        graph.setNodeColor(ownNodeId, colorCode);
    }

    private byte[] loadOwnDiscoveryData() {
        return storage.loadColor();
    }

    private void teardownPeers() {
        Graph graph = graphView.getGraph();
        graph.reset();
    }

    private void showColorPicker() {
        byte[] colorData = storage.loadColor();
        int colorCode = ColorStorage.getOrCreateColorCode(colorData, defaultColor);

        ColorPickerFragment fragment = ColorPickerFragment.newInstance(colorCode);
        fragment.show(getFragmentManager(), ColorPickerFragment.FRAGMENT_TAG);
    }

    @Override
    public void onColorPicked(int colorCode) {
        updateOwnDiscoveryInfo(colorCode);
    }

    private void handleStatusResult(final StatusResult statusResult) {

        String description = "";

        if (statusResult.getStatusCode() == StatusResult.INVALID_APP_KEY) {
            description = "Invalid app key";
        }
        else if (statusResult.getStatusCode() == StatusResult.INVALID_APPLICATION_ID) {
            description = "Invalid application ID";
        }
        else if (statusResult.getStatusCode() == StatusResult.INCOMPATIBLE_CLIENT_VERSION) {
            description = "Incompatible p2pkit (SDK) version, please update";
        }
        else if (statusResult.getStatusCode() == StatusResult.SERVER_CONNECTION_UNAVAILABLE) {
            description = "Server connection not available";
        }

        showError(description, true);
    }

    private void handleDiscoveryStateChange(final int state) {

        if (state == DiscoveryListener.STATE_OFF) {
            return;
        }

        if ((state & DiscoveryListener.STATE_LOCATION_PERMISSION_NOT_GRANTED) == DiscoveryListener.STATE_LOCATION_PERMISSION_NOT_GRANTED) {
            Toast.makeText(this, R.string.p2pkit_state_no_location_permission, Toast.LENGTH_LONG).show();
        }
        else if ((state & DiscoveryListener.STATE_SERVER_CONNECTION_UNAVAILABLE) == DiscoveryListener.STATE_SERVER_CONNECTION_UNAVAILABLE) {
            Toast.makeText(this, R.string.p2pkit_state_offline, Toast.LENGTH_LONG).show();
        }
        else if (state != DiscoveryListener.STATE_ON) {
            Toast.makeText(this, R.string.p2pkit_state_suspended, Toast.LENGTH_LONG).show();
        }
    }

    private void showConsole() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(ConsoleFragment.FRAGMENT_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ConsoleFragment fragment = ConsoleFragment.newInstance();
        fragment.show(ft, ConsoleFragment.FRAGMENT_TAG);
    }

    private void showError(final String message, final boolean retry) {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(this).setTitle("p2pkit Error").setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        if (retry) {
            builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.enableP2PKit();
                }
            });
        }

        builder.create().show();
    }

    private void hideSystemBars() {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

}
