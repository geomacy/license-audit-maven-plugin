package org.heneveld.maven.license_audit.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.model.License;
import org.eclipse.sisu.Nullable;

public class LicenseCodes {

    final static Map<String,String> KNOWN_LICENSE_CODES_WITH_REGEX = new LinkedHashMap<String, String>();
    final static Map<String,License> KNOWN_LICENSE_CODES_WITH_LICENSE = new LinkedHashMap<String, License>();
    
    protected static void addCodeWithRegex(String code, License l, String regex) {
        KNOWN_LICENSE_CODES_WITH_LICENSE.put(code, l);
        if (regex!=null) KNOWN_LICENSE_CODES_WITH_REGEX.put(code, regex.toLowerCase());
    }
        
    protected static String optionally(String pattern) {
        return "("+pattern+")?";
    }
    protected static String anyOf(String ...patterns) {
        String result = "("+patterns[0];
        for (int i=1; i<patterns.length; i++)
            result += "|"+patterns[i];
        return result+")";
    }
    protected static String anyNumberOf(String pattern) {
        return "("+pattern+")*";
    }
    protected static String orReversed(String w1, String sep, String w2) {
        return anyOf(w1+sep+w2, w2+sep+w1);
    }
    
    protected static final String SEPARATOR_PATTERN = "[\\-, \\(\\)\\[\\]<>/]*";
    protected final static String NO_VERSION_REGEX = SEPARATOR_PATTERN+
        orReversed(anyOf("no", "not", "un", "missing"), SEPARATOR_PATTERN, "v(\\.|ersion(ed)?)?")+SEPARATOR_PATTERN;

    protected static String version(String v) {
        String result = SEPARATOR_PATTERN+"(v(ersion|\\.)?)? *";
        while (!v.matches("(\\.0)*")) {
            String c = v.substring(0, 1);
            if (".".equals(c)) result+="\\.";
            else result += c;
            v = v.substring(1);
        }
        result += "(\\.0)*";
        return result;
    }

    protected static String licenseNameRegex(String code, String namePattern, @Nullable String version) {
        String codeWithoutVersion = code;
        if (version!=null) {
            for (int i=0; i<code.length(); i++) {
                if (code.substring(i).matches(version(version))) {
                    codeWithoutVersion = code.substring(0, i);
                    break;
                }
            }
        }
        String optionalParenthesizedCode = optionally(SEPARATOR_PATTERN + 
            "\\(?"+codeWithoutVersion+(version==null || version.isEmpty() ? "" : optionally(version(version)))+"\\)?");
        return
            anyOf(codeWithoutVersion, namePattern + optionalParenthesizedCode)
            + (version==null || version.isEmpty() ? optionally(NO_VERSION_REGEX) : version(version)) + optionalParenthesizedCode;
    }

    protected static String theLicense(String mainName) {
        return optionally("the ")+mainName+optionally(" license");
    }

    public static License newLicense(String name, String url, String comments) {
        License result = new License();
        result.setName(name);
        result.setUrl(url);
        result.setComments(comments);
        return result;        
    }

    public static void addCodeFromMainNamePattern(String code, String version, String name, String url, String mainName) {
        addCodeWithRegex(code, newLicense(name, url, null), licenseNameRegex(code, theLicense(mainName), version));        
    }

    // see http://opensource.org/licenses
//    Apache License 2.0
//    Eclipse Public License
//    Common Development and Distribution License
//    MIT license
//    GNU General Public License (GPL)
//    GNU Library or "Lesser" General Public License (LGPL)
//    Mozilla Public License 2.0
//    BSD 3-Clause "New" or "Revised" license
//    BSD 2-Clause "Simplified" or "FreeBSD" license
    
    static {
        addCodeFromMainNamePattern("ASL2", "2.0", "Apache License, version 2.0", "http://www.apache.org/licenses/LICENSE-2.0",  
            "apache"+anyNumberOf(anyOf(" public", " software")));
        addCodeFromMainNamePattern("ASL", null, "Apache License", "http://www.apache.org/licenses",  
            "apache"+anyNumberOf(anyOf(" public", " software")));
        
        addCodeFromMainNamePattern("EPL1", "1.0", "Eclipse Public License, version 1.0", "http://www.eclipse.org/legal/epl-v10.html",  
            "eclipse"+anyNumberOf(anyOf(" public", " software")));
        addCodeFromMainNamePattern("EPL", null, "Eclipse Public License", "http://www.eclipse.org/legal/epl-v10.html",  
            "eclipse"+anyNumberOf(anyOf(" public", " software")));

        addCodeFromMainNamePattern("CPL1", "1.0", "Common Public License, version 1.0", "http://opensource.org/licenses/cpl1.0.php",  
            "common public"+anyNumberOf(anyOf(" software")));
        addCodeFromMainNamePattern("CPL", null, "Common Public License", "http://opensource.org/licenses/cpl1.0.php",  
            "common public"+anyNumberOf(anyOf(" software")));

        addCodeFromMainNamePattern("MIT", null, "The MIT License", "http://opensource.org/licenses/MIT",  
            "MIT"+anyNumberOf(anyOf(" software")));
        
        addCodeFromMainNamePattern("CDDL1.1", "1.1", "Common Development and Distribution License, version 1.1", "https://glassfish.java.net/public/CDDL+GPL_1_1.html", 
            "common development"+anyNumberOf(anyOf("\\+", "\\-", "/", " ", "&", "and"))+"distribution");
        addCodeFromMainNamePattern("CDDL1", "1.0", "Common Development and Distribution License, version 1.0", "http://opensource.org/licenses/CDDL-1.0",  
            "common development"+anyNumberOf(anyOf("\\+", "\\-", "/", " ", "&", "and"))+"distribution");
        addCodeFromMainNamePattern("CDDL", null, "Common Development and Distribution License", "http://opensource.org/licenses/CDDL-1.0",  
            "common development"+anyNumberOf(anyOf("\\+", "\\-", "/", " ", "&", "and"))+"distribution");

        addCodeFromMainNamePattern("GPL3", "3.0", "GNU General Public License, version 3.0", "http://www.gnu.org/licenses/gpl-3.0.html",  
            "gnu"+anyNumberOf(anyOf(" general", " public")));
        addCodeFromMainNamePattern("GPL2", "2.0", "GNU General Public License, version 2.0", "http://www.gnu.org/licenses/gpl-2.0.html",  
            "gnu"+anyNumberOf(anyOf(" general", " public")));
        addCodeFromMainNamePattern("GPL", null, "GNU General Public License", "http://www.gnu.org/licenses/gpl.html",  
            "gnu"+anyNumberOf(anyOf(" general", " public")));

        addCodeFromMainNamePattern("LGPL3", "3.0", "GNU Lesser General Public License, version 3.0", "http://www.gnu.org/licenses/lgpl-3.0.html",  
            orReversed("lesser", " ", "gnu")+anyNumberOf(anyOf(" general", " public")));
        addCodeFromMainNamePattern("LGPL2.1", "2.1", "GNU Lesser General Public License, version 2.1", "http://www.gnu.org/licenses/lgpl-2.0.html",  
            orReversed("lesser", " ", "gnu")+anyNumberOf(anyOf(" general", " public")));
        addCodeFromMainNamePattern("LGPL2", "2.0", "GNU Lesser General Public License, version 2.0", "http://www.gnu.org/licenses/lgpl-2.0.html",  
            orReversed("lesser", " ", "gnu")+anyNumberOf(anyOf(" general", " public")));
        addCodeFromMainNamePattern("LGPL", null, "GNU Lesser General Public License", "http://www.gnu.org/licenses/lgpl.html",  
            orReversed("lesser", " ", "gnu")+anyNumberOf(anyOf(" general", " public")));

        addCodeFromMainNamePattern("MPL2", "2.0", "Mozilla Public License, version 2.0", "https://www.mozilla.org/MPL/2.0/",  
            "mozilla"+anyNumberOf(anyOf(" public", " software")));
        addCodeFromMainNamePattern("MPL", null, "Mozilla Public License", "https://www.mozilla.org/MPL/",  
            "mozilla"+anyNumberOf(anyOf(" public", " software")));
        
        String bsd3Words = anyOf("3"+SEPARATOR_PATTERN+"clause", "new", "revised", "modified");
        String bsd2Words = anyOf("2"+SEPARATOR_PATTERN+"clause", "simplified", "freebsd");
        String bsdPadding = anyNumberOf(anyOf(SEPARATOR_PATTERN, "like", "style", "bsd"));
        String bsd3Padding = anyNumberOf(anyOf(bsdPadding, bsd3Words));
        String bsd2Padding = anyNumberOf(anyOf(bsdPadding, bsd2Words));
        addCodeWithRegex("BSD-3-Clause", newLicense("The BSD 3-Clause (New BSD) License", "http://opensource.org/licenses/BSD-3-Clause", null),
            bsd3Padding+theLicense(bsd3Padding+orReversed("bsd", SEPARATOR_PATTERN, bsd3Words)+bsd3Padding)+bsd3Padding);
        addCodeWithRegex("BSD-2-Clause", newLicense("The BSD 2-Clause (FreeBSD) License", "http://opensource.org/licenses/BSD-2-Clause", null),
            bsd2Padding+theLicense(bsd2Padding+anyOf("freebsd", orReversed("bsd", SEPARATOR_PATTERN, bsd2Words))+bsd2Padding)+bsd2Padding);
        addCodeWithRegex("BSD", newLicense("BSD License", "http://opensource.org/licenses/BSD-2-Clause", null),
            bsdPadding+theLicense(bsdPadding+"bsd"+bsdPadding)+bsdPadding+optionally(NO_VERSION_REGEX));
    }
    
    public static Map<String, String> getKnownLicenseCodesWithRegexes() {
        return Collections.unmodifiableMap(KNOWN_LICENSE_CODES_WITH_REGEX);
    }
    
    /** Returns a license code for known license,
     * empty string if it looks like there is no license info, 
     * or null if it could not extract a code */ 
    public static String getLicenseCode(String licenseSummary) {
        if (licenseSummary==null || licenseSummary.length()==0) return "";
        
        String ls = licenseSummary.toLowerCase().trim();
        if (ls.matches("<no[ a-z]*>")) return "";

        for (Map.Entry<String,String> l: KNOWN_LICENSE_CODES_WITH_REGEX.entrySet()) {
            if (ls.matches(l.getValue())) return l.getKey();
            if (ls.equals(l.getKey().toLowerCase())) return l.getKey();
        }
        return null;
    }

    public static License lookupCode(String code) {
        if (code==null) return null;
        return KNOWN_LICENSE_CODES_WITH_LICENSE.get(code.trim());
    }

}
