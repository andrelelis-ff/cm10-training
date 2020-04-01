#### Attention

do not delete this folder, it get copied by the dockerfile as empty folder using fixed owner and group

this line
```
COPY --chown=coremedia:coremedia src/docker /coremedia
```
