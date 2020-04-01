<#-- @ftlvariable name="self" type="com.coremedia.blueprint.nuggad.Nuggad" -->

<#if self.enabled>
    <script type="text/javascript">

        var cookieName = "nuggAIArray";
        var cookiePath = "/";
        var cookieTTL = 5 * 60 * 1000; // 5 minutes


        function setNuggAdCookie(value) {
            var date = new Date();
            date.setTime(date.getTime() + cookieTTL);
            var expires = "; expires=" + date.toGMTString();
            document.cookie = cookieName + "=" + encodeURIComponent(value) + expires + "; path=" + cookiePath;
        }

        function readNuggAdCookie() {
            var nameEQ = cookieName + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') c = c.substring(1, c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
            }
            return null;
        }

        function hasNuggAdCookie() {
            return readNuggAdCookie() != null;
        }

    </script>
    <script type="text/javascript">
        var nuggrid = encodeURIComponent(top.location.href);
        document.write('<script type=\"text/javascript\" src=\"http://coremedia.nuggad.net/rc?nuggn=' + ${self.nuggn} +'&amp;nuggsid=' + ${self.nuggsid} +'&amp;nuggrid=' + nuggrid + '"><\/script>');
    </script>
    <script type="text/javascript">
        var initialRequest = !hasNuggAdCookie();
        setNuggAdCookie(nuggAIArray.join());
        if (initialRequest && hasNuggAdCookie() <#--/* make sure we have written cookie successfully */--> && document.location.href.indexOf("webflow") == -1 <#--/* Do not reload on webflow requests, since they might be the registration */ -->) {
            window.location.reload();
        }
    </script>
</#if>