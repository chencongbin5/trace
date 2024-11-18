package com.akuchen.trace.parse.jdi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {

    Integer notifyCompleted() throws RemoteException;
}
