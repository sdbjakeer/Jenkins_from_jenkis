/*
 * The MIT License
 *
 * Copyright (c) 2010, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.util.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 *
 * <pre>
typedef struct _SHELLEXECUTEINFO {
  DWORD     cbSize;
  ULONG     fMask;
  HWND      hwnd;
  LPCTSTR   lpVerb;
  LPCTSTR   lpFile;
  LPCTSTR   lpParameters;
  LPCTSTR   lpDirectory;
  int       nShow;
  HINSTANCE hInstApp;
  LPVOID    lpIDList;
  LPCTSTR   lpClass;
  HKEY      hkeyClass;
  DWORD     dwHotKey;
  union {
    HANDLE hIcon;
    HANDLE hMonitor;
  } DUMMYUNIONNAME;
  HANDLE    hProcess;
} SHELLEXECUTEINFO, *LPSHELLEXECUTEINFO;
 * </pre>
 * @author Kohsuke Kawaguchi
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb759784(v=VS.85).aspx">MSDN: SHELLEXECUTEINFO</a>
 */
public class SHELLEXECUTEINFO extends Structure {
    public int cbSize = size();
    public int fMask;
    public Pointer hwnd;
    public String lpVerb;
    public String lpFile;
    public String lpParameters;
    public String lpDirectory;
    public int nShow = 1;
    public Pointer hInstApp;
    public Pointer lpIDList;
    public String lpClass;
    public Pointer hkeyClass;
    public int dwHotKey;
    public Pointer hIcon;
    public Pointer hProcess;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("cbSize", "fMask", "hwnd",
                "lpVerb", "lpFile", "lpParameters", "lpDirectory",
                "nShow", "hInstApp", "lpIDList", "lpClass",
                "hkeyClass", "dwHotKey", "hIcon", "hProcess");
    }

    public static final int SEE_MASK_NOCLOSEPROCESS = 0x40;
    public static final int SW_HIDE = 0;
    public static final int SW_SHOW = 0;
}
