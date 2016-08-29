/*___Generated_by_IDEA___*/

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\eSdk\\raw_src_code_no_svn\\src\\com\\huawei\\push\\ipc\\Ipush.aidl
 */
package com.huawei.push.ipc;
public interface Ipush extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.huawei.push.ipc.Ipush
{
private static final java.lang.String DESCRIPTOR = "com.huawei.push.ipc.Ipush";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.huawei.push.ipc.Ipush interface,
 * generating a proxy if needed.
 */
public static com.huawei.push.ipc.Ipush asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.huawei.push.ipc.Ipush))) {
return ((com.huawei.push.ipc.Ipush)iin);
}
return new com.huawei.push.ipc.Ipush.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_startPush:
{
data.enforceInterface(DESCRIPTOR);
com.huawei.push.ipc.IPushConfig _arg0;
if ((0!=data.readInt())) {
_arg0 = com.huawei.push.ipc.IPushConfig.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.startPush(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_resume:
{
data.enforceInterface(DESCRIPTOR);
this.resume();
reply.writeNoException();
return true;
}
case TRANSACTION_end:
{
data.enforceInterface(DESCRIPTOR);
this.end();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.huawei.push.ipc.Ipush
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void startPush(com.huawei.push.ipc.IPushConfig config) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((config!=null)) {
_data.writeInt(1);
config.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_startPush, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void resume() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_resume, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void end() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_end, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_startPush = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_resume = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_end = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void startPush(com.huawei.push.ipc.IPushConfig config) throws android.os.RemoteException;
public void resume() throws android.os.RemoteException;
public void end() throws android.os.RemoteException;
}
