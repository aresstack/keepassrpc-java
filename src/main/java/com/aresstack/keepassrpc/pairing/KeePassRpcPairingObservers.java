package com.aresstack.keepassrpc.pairing;

/**
 * Factory methods for pairing observers.
 */
public final class KeePassRpcPairingObservers {
    private static final KeePassRpcPairingObserver NO_OP = new KeePassRpcPairingObserver() {
        @Override
        public void onPairingStarted(KeePassRpcPairingRequest request) {
        }

        @Override
        public void onKeePassConnectionWaiting(KeePassRpcEndpoint endpoint) {
        }

        @Override
        public void onKeePassUserConfirmationRequired(KeePassRpcPairingSession session) {
        }

        @Override
        public void onPairingSucceeded(KeePassRpcPairingResult result) {
        }

        @Override
        public void onPairingFailed(KeePassRpcPairingException failure) {
        }
    };

    private KeePassRpcPairingObservers() {
    }

    public static KeePassRpcPairingObserver none() {
        return NO_OP;
    }
}
