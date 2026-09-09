package kawaii.viey.browser;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class filesProvider extends DocumentsProvider {
	public static final String[] rootProjection = {"root_id", "mime_types", "flags", "icon", "title", "summary", "document_id"};
	public static final String[] docProjection = {"document_id", "mime_type", "_display_name", "last_modified", "flags", "_size", "mt_extras"};
	public String pkgName;
	public File appDataDir;
	public File userDeDir;
	public File extDataDir;
	public File obbDir;
	public File sourceDir;
	public File downDir;
    
	public static boolean deleteRecursive(File file) throws FileNotFoundException {
		if (file == null) return false;
		
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			if (listFiles != null) {
				for (File child : listFiles) {
					if (!deleteRecursive(child)) {
						return false;
					}
				}
			}
		}
		return file.delete();
	}
	
	public static String getMimeType(File file) {
		if (file.isDirectory()) {
			return "vnd.android.document/directory";
		}
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(46);
		if (lastIndexOf >= 0) {
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastIndexOf + 1).toLowerCase());
			if (mimeType != null) {
				return mimeType;
			}
		}
		return "application/octet-stream";
	}
	
	@Override
	public final void attachInfo(Context context, ProviderInfo providerInfo) {
		super.attachInfo(context, providerInfo);
		this.pkgName = context.getPackageName();
		File parentFile = context.getFilesDir().getParentFile();
		this.appDataDir = parentFile;
		String path = parentFile.getPath();
		if (path.startsWith("/data/user/")) {
			this.userDeDir = new File("/data/user_de/" + path.substring(11));
		}
		File externalFilesDir = context.getExternalFilesDir(null);
		if (externalFilesDir != null) {
			this.extDataDir = externalFilesDir.getParentFile();
		}
		this.obbDir = context.getObbDir();
		this.sourceDir = new File("/storage/emulated/0/iEpp/ProjectApp/n/ProjectOpp/"+context.getPackageName()+"/");
        this.downDir = new File(VieYApp.getDownloadPath(context));
	}
	
	public final File resolveFile(String docId, boolean checkExists) throws FileNotFoundException {
		String str;
		String substring2;
		if (!docId.startsWith(this.pkgName)) {
			throw new FileNotFoundException(docId.concat(" not found"));
		}
		String substring3 = docId.substring(this.pkgName.length());
		if (substring3.startsWith("/")) {
			substring3 = substring3.substring(1);
		}
		if (substring3.isEmpty()) {
			return null;
		}
		int indexOf = substring3.indexOf(47);
		if (indexOf == -1) {
			substring2 = "";
			str = substring3;
		} else {
			str = substring3.substring(0, indexOf);
			substring2 = substring3.substring(indexOf + 1);
		}
		File file;
		if (str.equalsIgnoreCase("data")) {
			file = new File(this.appDataDir, substring2);
		} else if (str.equalsIgnoreCase("extData") && this.extDataDir != null) {
			file = new File(this.extDataDir, substring2);
		} else if (str.equalsIgnoreCase("obb") && this.obbDir != null) {
			file = new File(this.obbDir, substring2);
		} else if (str.equalsIgnoreCase("source") && this.sourceDir != null) {
			file = new File(this.sourceDir, substring2);
		} else if (str.equalsIgnoreCase("userDe") && this.userDeDir != null) {
			file = new File(this.userDeDir, substring2);
		} else {
			throw new FileNotFoundException(docId.concat(" not found"));
		}
		if (!checkExists) {
			return file;
		}
		try {
			Os.lstat(file.getPath());
			return file;
		} catch (Exception e) {
			throw new FileNotFoundException(docId.concat(" not found"));
		}
	}
	
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		return super.call(method, arg, extras);
	}
	
	@Override
	public final String createDocument(String str, String str2, String str3) throws FileNotFoundException {
		StringBuilder sb;
		File targetDir = resolveFile(str, true);
		if (targetDir != null) {
			File newFile = new File(targetDir, str3);
			int i = 2;
			while (newFile.exists()) {
				newFile = new File(targetDir, str3 + " (" + i + ")");
				i++;
			}
			try {
				if ("vnd.android.document/directory".equals(str2) ? newFile.mkdir() : newFile.createNewFile()) {
					if (str.endsWith("/")) {
						StringBuilder sb2 = new StringBuilder();
						sb2.append(str);
						sb2.append(newFile.getName());
						sb = sb2;
					} else {
						StringBuilder sb3 = new StringBuilder();
						sb3.append(str);
						sb3.append("/");
						sb3.append(newFile.getName());
						sb = sb3;
					}
					return sb.toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new FileNotFoundException("Failed to create document in " + str + " with name " + str3);
	}
	
	public final void addDocRow(MatrixCursor cursor, String docId, File file) throws FileNotFoundException {
		String displayName;
		boolean isChild = false;
		if (file == null) {
			file = resolveFile(docId, true);
		}
		if (file == null) {
			MatrixCursor.RowBuilder newRow = cursor.newRow();
			newRow.add("document_id", this.pkgName);
			newRow.add("_display_name", this.pkgName);
			newRow.add("_size", 0L);
			newRow.add("mime_type", "vnd.android.document/directory");
			newRow.add("last_modified", 0);
			newRow.add("flags", 0);
			return;
		}
		
		int flags = 0;
		
		boolean selfWritable = file.canWrite();
		boolean parentWritable = (file.getParentFile() != null)
		&& file.getParentFile().canWrite();
	
		if (file.isDirectory()) {
			if (selfWritable) {
				flags |= 8;
			}
		} else {
			if (selfWritable) {
				flags |= 2;
			}
		}
		
		if (parentWritable || selfWritable) {
			flags |= 4;
			flags |= 64;
			flags |= 256;
		}
		
		String path = file.getPath();
		if (path.equals(this.appDataDir.getPath())) {
			displayName = "data";
		} else if (this.extDataDir != null && path.equals(this.extDataDir.getPath())) {
			displayName = "extData";
		} else if (this.obbDir != null && path.equals(this.obbDir.getPath())) {
			displayName = "obb";
		} else if (this.userDeDir != null && path.equals(this.userDeDir.getPath())) {
			displayName = "userDe";
		} else if (this.sourceDir != null && path.equals(this.sourceDir.getPath())) {
			displayName = "source";
		} else {
			displayName = file.getName();
			isChild = true;
		}
		MatrixCursor.RowBuilder row = cursor.newRow();
		row.add("document_id", docId);
		row.add("_display_name", displayName);
		row.add("_size", Long.valueOf(file.length()));
		row.add("mime_type", getMimeType(file));
		row.add("last_modified", Long.valueOf(file.lastModified()));
		row.add("flags", Integer.valueOf(flags));
		row.add("mt_path", file.getAbsolutePath());
		if (isChild) {
			try {
				StringBuilder sb = new StringBuilder();
				StructStat lstat = Os.lstat(path);
				sb.append(lstat.st_mode);
				sb.append("|");
				sb.append(lstat.st_uid);
				sb.append("|");
				sb.append(lstat.st_gid);
				if ((lstat.st_mode & 61440) == 40960) {
					sb.append("|");
					sb.append(Os.readlink(path));
				}
				row.add("mt_extras", sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public final void deleteDocument(String docId) throws FileNotFoundException {
		File file = resolveFile(docId, true);
		if (file == null || !deleteRecursive(file)) {
			throw new FileNotFoundException("Failed to delete document ".concat(docId));
		}
	}
	
	@Override
	public final String getDocumentType(String docId) throws FileNotFoundException {
		File file = resolveFile(docId, true);
		return file == null ? "vnd.android.document/directory" : getMimeType(file);
	}
	
	@Override
	public final boolean isChildDocument(String parentId, String childId) {
		return childId.startsWith(parentId);
	}
	
	@Override
	public final String moveDocument(String srcId, String srcParentId, String dstParentId) throws FileNotFoundException {
		File srcFile = resolveFile(srcId, true);
		File dstDir = resolveFile(dstParentId, true);
		if (srcFile != null && dstDir != null) {
			File dstFile = new File(dstDir, srcFile.getName());
			if (!dstFile.exists() && srcFile.renameTo(dstFile)) {
				if (dstParentId.endsWith("/")) {
					return dstParentId + dstFile.getName();
				}
				return dstParentId + "/" + dstFile.getName();
			}
		}
		throw new FileNotFoundException("Failed to move document " + srcId + " to " + dstParentId);
	}
	
	@Override
	public final boolean onCreate() {
		return true;
	}
	
	@Override
	public final ParcelFileDescriptor openDocument(String docId, String mode, CancellationSignal signal) throws FileNotFoundException {
		File file = resolveFile(docId, false);
		if (file != null) {
			return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
		}
		throw new FileNotFoundException(docId.concat(" not found"));
	}
	
	@Override
	public final Cursor queryChildDocuments(String parentId, String[] projection, String sortOrder) throws FileNotFoundException {
		if (parentId.endsWith("/")) {
			parentId = parentId.substring(0, parentId.length() - 1);
		}
		if (projection == null) {
			projection = docProjection;
		}
		MatrixCursor cursor = new MatrixCursor(projection);
		File parentFile = resolveFile(parentId, true);
		if (parentFile == null) {
			addDocRow(cursor, parentId.concat("/data"), this.appDataDir);
			File ext = this.extDataDir;
			if (ext != null && ext.exists()) {
				addDocRow(cursor, parentId.concat("/extData"), this.extDataDir);
			}
			File obb = this.obbDir;
			if (obb != null && obb.exists()) {
				addDocRow(cursor, parentId.concat("/obb"), this.obbDir);
			}
			File source = this.sourceDir;
			if (source != null && source.exists()) {
				addDocRow(cursor, parentId.concat("/source"), this.sourceDir);
			}
			File de = this.userDeDir;
			if (de != null && de.exists()) {
				addDocRow(cursor, parentId.concat("/userDe"), this.userDeDir);
			}
		} else {
			File[] listFiles = parentFile.listFiles();
			if (listFiles != null) {
				for (File child : listFiles) {
					addDocRow(cursor, parentId + "/" + child.getName(), child);
				}
			}
		}
		return cursor;
	}
	
	@Override
	public final Cursor queryDocument(String docId, String[] projection) throws FileNotFoundException {
		if (projection == null) {
			projection = docProjection;
		}
		MatrixCursor cursor = new MatrixCursor(projection);
		addDocRow(cursor, docId, null);
		return cursor;
	}
	
	@Override
	public final Cursor queryRoots(String[] projection) {
		if (projection == null) {
			projection = rootProjection;
		}
		MatrixCursor cursor = new MatrixCursor(projection);
		ApplicationInfo appInfo = getContext().getApplicationInfo();
		MatrixCursor.RowBuilder row = cursor.newRow();
		row.add("root_id", this.pkgName);
		row.add("document_id", this.pkgName);
		row.add("summary", this.pkgName);
		row.add("flags", 17);
		row.add("title", "VieY Browser Dir");
		row.add("mime_types", "*/*");
		row.add("icon", Integer.valueOf(appInfo.icon));
		return cursor;
	}
	
	
	@Override
	public final void removeDocument(String docId, String parentDocId) {
		try {
			deleteDocument(docId);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public final String renameDocument(String docId, String newName) throws FileNotFoundException {
		File file = resolveFile(docId, true);
		if (file == null || !file.renameTo(new File(file.getParentFile(), newName))) {
			throw new FileNotFoundException("Failed to rename document " + docId + " to " + newName);
		}
		return docId.substring(0, docId.lastIndexOf(47, docId.length() - 2)) + "/" + newName;
	}
}