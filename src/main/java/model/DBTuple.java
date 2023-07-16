package model;

import com.scalar.db.api.Delete;
import com.scalar.db.api.DeleteBuilder;
import com.scalar.db.api.Get;
import com.scalar.db.api.GetBuilder;
import com.scalar.db.api.Put;
import com.scalar.db.api.PutBuilder;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.ScanBuilder;
import com.scalar.db.io.Key;

public abstract class DBTuple
{
    public abstract String getNamespace();
    public abstract String getTable();

    protected abstract Key getPartitionKey();

    public abstract void update(Result tuple);
    
    public GetBuilder.PartitionKeyOrIndexKey getGetBuilder()
    {
        return Get.newBuilder().namespace(getNamespace()).table(getTable());
    }
    
    public ScanBuilder.PartitionKeyOrIndexKeyOrAll getScanBuilder()
    {
        return Scan.newBuilder().namespace(getNamespace()).table(getTable());
    }

    public PutBuilder.PartitionKey getPutBuilder()
    {
        return Put.newBuilder().namespace(getNamespace()).table(getTable());
    }

    public DeleteBuilder.PartitionKey getDeleteBuilder()
    {
        return Delete.newBuilder().namespace(getNamespace()).table(getTable());
    }

    public Get getGet()
    {
        return getGetBuilder().partitionKey(getPartitionKey()).build();
    }

    protected abstract PutBuilder.Buildable completePut(PutBuilder.Buildable put);

    public Put getPut()
    {
        return completePut(getPutBuilder().partitionKey(getPartitionKey())).build();
    }
    
    public Delete getDelete()
    {
        return getDeleteBuilder().partitionKey(getPartitionKey()).build();
    }

    public Scan getScanAll()
    {
        return getScanBuilder().all().build();
    }
}
